package com.gatekeeper.services;

import com.gatekeeper.entity.ApiTokens;
import com.gatekeeper.repos.ApiTokensRepository;
import com.gatekeeper.validators.ValidationCompleteEvent;
import java.util.Optional;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 *
 * @author null
 */
@Service
public class KeyFetcherService {

    private final ApiTokensRepository apiTokensRepository;
    private final CacheManager cacheManager;
    private boolean isValidationComplete = false;

    public KeyFetcherService(ApiTokensRepository apiTokensRepository, CacheManager cacheManager) {
        this.apiTokensRepository = apiTokensRepository;
        this.cacheManager = cacheManager;
    }

    @EventListener
    public void onValidationComplete(ValidationCompleteEvent event) {
        this.isValidationComplete = true;
    }
    
    public boolean apiKeyValidator(String requestKey) {
        if (isValidationComplete) {
            Cache cache = cacheManager.getCache("gatekeeper");
            if (cache != null) {
                Cache.ValueWrapper cachedValue = cache.get(requestKey);
                if (cachedValue != null) {
                    return (boolean) cachedValue.get();
                }

                Optional<ApiTokens> token = apiTokensRepository.findByUserTokens(requestKey);
                boolean isValid = token.isPresent();
                if (isValid) {
                    cache.put(requestKey, true);
                }
                return isValid;
            }
        }
        return false;
    }
}
