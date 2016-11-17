package org.zalando.zmon.notifications.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Created by jmussler on 17.11.16.
 */
public class ChainedTokenInfo implements TokenInfoService {

    private final Logger log = LoggerFactory.getLogger(ChainedTokenInfo.class);

    TokenInfoService[] services;

    public ChainedTokenInfo(TokenInfoService... service) {
            services = service;
            for(TokenInfoService s : services) {
                log.info("TokenInfo entry: {}", s.getClass());
            }
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
