package com.gatekeeper.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gatekeeper.config.Constants;
import com.gatekeeper.events.ValidationCompleteEvent;
import com.gatekeeper.services.KeyFetcherService;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 *
 * @author null
 */
@Component
public class ProxyHandler implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(ProxyHandler.class);
    private final WebClient webClient;
    private final KeyFetcherService keyFetcherService;
    private final RateLimiter rateLimiter;
    private final SpringShutdownUtil shutdownUtil;

    private volatile boolean isValidationComplete = false;
    private List<String> proxyTargets;
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Value("${PROXY_TARGET_URL:}")
    private String envTargetUrl;

    @Value("${load.balance.mode}")
    private String loadBalanceMode;

    @Value("${rate.limit.enabled}")
    private boolean rateLimitEnabled;

    public ProxyHandler(WebClient.Builder webClientBuilder, KeyFetcherService keyFetcherService,
            RateLimiter rateLimiter, SpringShutdownUtil shutdownUtil) {
        this.webClient = webClientBuilder.build();
        this.keyFetcherService = keyFetcherService;
        this.rateLimiter = rateLimiter;
        this.shutdownUtil = shutdownUtil;
    }

    @EventListener
    public void onValidationComplete(ValidationCompleteEvent event) {
        this.isValidationComplete = true;
        initializeProxyTargets();
    }

    private void initializeProxyTargets() {
        if (envTargetUrl != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                this.proxyTargets = objectMapper.readValue(envTargetUrl, new TypeReference<List<String>>() {});
                proxyTargets.forEach(url -> logger.info("Proxy URL: " + url));
            } catch (IOException e) {
                logger.error(String.format(Constants.ERR_PARSE_TARG_FAIL, Constants.ENV_PROXY_TARGET_URL));
                shutdownUtil.shutDownSpringApp();
            }
        }
    }

    private String getNextProxyTarget() {
        if (proxyTargets == null || proxyTargets.isEmpty()) {
            return null;
        }
        int index = currentIndex.getAndUpdate(i -> (i + 1) % proxyTargets.size());
        return proxyTargets.get(index);
    }

    private String getIpHashProxyTarget(String clientIp) {
        if (proxyTargets == null || proxyTargets.isEmpty() || clientIp == null) {
            return null;
        }
        int hash = Math.abs(clientIp.hashCode());
        int index = hash % proxyTargets.size();
        return proxyTargets.get(index);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!isValidationComplete) {
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            String errorMessage = Constants.ERR_SERVICE_VAL_FAIL;
            logger.error(errorMessage);

            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorMessage.getBytes());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }

        String apiKeyHeader = exchange.getRequest().getHeaders().getFirst(Constants.KEY_HEADER);

        if (rateLimitEnabled && !rateLimiter.tryConsume(apiKeyHeader)) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            String errorMessage = Constants.ERR_RATE_LIMIT_EXCEEDED;
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorMessage.getBytes());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }

        debugLogRequest(exchange, apiKeyHeader);

        if (apiKeyHeader == null || !keyFetcherService.apiKeyValidator(apiKeyHeader)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String path = exchange.getRequest().getURI().getPath();
        String query = exchange.getRequest().getURI().getQuery();
        
        String targetUrl;
        InetSocketAddress remoteAddr = exchange.getRequest().getRemoteAddress();
        String clientIp = (remoteAddr != null) ? remoteAddr.getAddress().getHostAddress() : null;
        
         if (clientIp == null && loadBalanceMode.equals(Constants.OPT_LB_IPH)) {
            logger.error(Constants.ERR_NO_CLIENT_IP);
            targetUrl = getNextProxyTarget();
        } else {
            if (loadBalanceMode.equals(Constants.OPT_LB_IPH)) {
                targetUrl = getIpHashProxyTarget(clientIp);
            } else {
                targetUrl = getNextProxyTarget();
            }
        }

        if (targetUrl == null) {
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(Constants.ERR_NO_PROXY_TARGET.getBytes());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
        
        String url = targetUrl + path + (query != null ? "?" + query : "");

        HttpMethod method = exchange.getRequest().getMethod();
        HttpHeaders headers = exchange.getRequest().getHeaders();

        return webClient.method(method)
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .body(BodyInserters.fromDataBuffers(exchange.getRequest().getBody()))
                .exchangeToMono(clientResponse -> {
                    exchange.getResponse().setStatusCode(clientResponse.statusCode());
                    exchange.getResponse().getHeaders().putAll(clientResponse.headers().asHttpHeaders());

                    logger.info(String.format(Constants.MSG_REQEUST_FWD, url));
                    return exchange.getResponse().writeWith(clientResponse.bodyToFlux(DataBuffer.class));
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    exchange.getResponse().setStatusCode(ex.getStatusCode());

                    logger.error(String.format(Constants.ERR_REQUEST_FWD_FAIL, ex.getMessage()));
                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                            .bufferFactory().wrap(ex.getResponseBodyAsByteArray())));
                })
                .onErrorResume(Exception.class, ex -> {
                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                    logger.error(String.format(Constants.ERR_REQUEST_FWD_FAIL, ex.getMessage()));

                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(Constants.ERR_SVC_NOT_AVAILABLE.getBytes());
                    return exchange.getResponse().writeWith(Mono.just(buffer));
                });
    }

    private void debugLogRequest(ServerWebExchange exchange, String apiKey) {
        String method = exchange.getRequest().getMethodValue();
        String uri = exchange.getRequest().getURI().toString();
        InetSocketAddress remoteAddr = exchange.getRequest().getRemoteAddress();
        String clientIp = "null";
        if (remoteAddr != null) {
            clientIp = remoteAddr.toString();
        }
        logger.trace("Request received: method={}, uri={}, apiKey={}, clientIp={}", method, uri, apiKey, clientIp);
    }
}
