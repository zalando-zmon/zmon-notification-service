package org.zalando.zmon.notifications.oauth;

import java.util.Optional;

/**
 * Created by jmussler on 17.11.16.
 */
public class ChainedTokenInfo implements TokenInfoService {

    TokenInfoService[] services;

    public ChainedTokenInfo(TokenInfoService... service) {
            services = service;
    }

    @Override
    public Optional<String> lookupUid(String authorizationHeaderValue) {
        if (null == authorizationHeaderValue) {
            return Optional.empty();
        }

        for(TokenInfoService service : services) {
            Optional<String> uid = service.lookupUid(authorizationHeaderValue);
            if (uid.isPresent()) {
                return uid;
            }
        }
        return Optional.empty();
    }
}
