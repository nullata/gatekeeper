package com.gatekeeper.services;

import com.gatekeeper.components.EnvironmentUtils;
import com.gatekeeper.components.SpringShutdownUtil;
import com.gatekeeper.config.Constants;
import com.gatekeeper.dtos.Gatekey;
import com.gatekeeper.events.ValidationCompleteEvent;
import com.gatekeeper.exceptions.EnvironmentValidationException;
import com.gatekeeper.repos.GateKeyRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 *
 * @author null
 */
@Service
public class KeyFetcherService {

    private static final Logger logger = LoggerFactory.getLogger(KeyFetcherService.class);
    private final CacheManager cacheManager;
    private final GateKeyRepository gateKeyRepository;
    private final EnvironmentUtils environmentUtils;
    private final SpringShutdownUtil shutdownUtil;
    private volatile boolean isValidationComplete = false;

    public KeyFetcherService(CacheManager cacheManager,
            GateKeyRepository gateKeyRepository,
            EnvironmentUtils environmentUtils,
            SpringShutdownUtil shutdownUtil) {
        this.cacheManager = cacheManager;
        this.gateKeyRepository = gateKeyRepository;
        this.environmentUtils = environmentUtils;
        this.shutdownUtil = shutdownUtil;
    }

    @EventListener
    public void onValidationComplete(ValidationCompleteEvent event) {
        this.isValidationComplete = true;
    }

    public boolean apiKeyValidator(String requestKey) {
        if (isValidationComplete) {
            Cache cache = cacheManager.getCache(Constants.APP_NAME);
            if (cache != null) {
                Cache.ValueWrapper cachedValue = cache.get(requestKey);
                if (cachedValue != null) {
                    return (boolean) cachedValue.get();
                }
                String table = "", column = "";
                try {
                    table = environmentUtils.validateEnvVar(Constants.ENV_DB_TABLE);
                    column = environmentUtils.validateEnvVar(Constants.ENV_DB_COLUMN);

                    Optional<Gatekey> gatekey = Optional.ofNullable(gateKeyRepository.findByKey(table, column, requestKey));
                    boolean isValid = gatekey.isPresent();
                    if (isValid) {
                        cache.put(requestKey, true);
                    }
                    return isValid;
                } catch (EnvironmentValidationException | DataAccessException ex) {
                    logger.error(ex.getMessage());
                    shutdownUtil.shutDownSpringApp();
                }
            }
        }
        return false;
    }
}
