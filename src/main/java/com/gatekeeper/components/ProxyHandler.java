package com.gatekeeper.components;

import com.gatekeeper.services.KeyFetcherService;
import com.gatekeeper.validators.ValidationCompleteEvent;
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
    
    private boolean isValidationComplete = false;
    private String targetUrl;
    
    @Value("${PROXY_TARGET_URL:}")
    private String envTargetUrl;
    

    public ProxyHandler(WebClient.Builder webClientBuilder, KeyFetcherService keyFetcherService,
        RateLimiter rateLimiter) {
        this.webClient = webClientBuilder.build();
        this.keyFetcherService = keyFetcherService;
        this.rateLimiter = rateLimiter;
    }
    
    @EventListener
    public void onValidationComplete(ValidationCompleteEvent event) {
        this.isValidationComplete = true;
        this.targetUrl = envTargetUrl;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!isValidationComplete) {
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            String errorMessage = "Service not started due to runtime validation issues";
            logger.error(errorMessage);
            
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorMessage.getBytes());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
        
        if (!rateLimiter.tryConsume()) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            String errorMessage = "Rate limit exceeded";
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorMessage.getBytes());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }        
        
        String apiKeyHeader = exchange.getRequest().getHeaders().getFirst("x-gate-key");
        
        debugLogRequest(exchange, apiKeyHeader);

        if (apiKeyHeader == null || !keyFetcherService.apiKeyValidator(apiKeyHeader)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String path = exchange.getRequest().getURI().getPath();
        String query = exchange.getRequest().getURI().getQuery();
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
                    
                    logger.trace("Forwarding request: " + url);
                    return exchange.getResponse().writeWith(clientResponse.bodyToFlux(DataBuffer.class));
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    exchange.getResponse().setStatusCode(ex.getStatusCode());
                    
                    logger.error("Could not forward request: " + ex.getMessage());
                    return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory().wrap(ex.getResponseBodyAsByteArray())));
                })
                .onErrorResume(Exception.class, ex -> {
                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                    String errorMessage = "Service Not Available";
                    logger.error("Could not forward request: " + ex.getMessage());
                    
                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorMessage.getBytes());
                    return exchange.getResponse().writeWith(Mono.just(buffer));
                });
    }
    
    private void debugLogRequest(ServerWebExchange exchange, String apiKey) {
        String method = exchange.getRequest().getMethodValue();
        String uri = exchange.getRequest().getURI().toString();
        String clientIp = exchange.getRequest().getRemoteAddress().toString();
        logger.debug("Request received: method={}, uri={}, apiKey={}, clientIp={}", method, uri, apiKey, clientIp);
    }
}