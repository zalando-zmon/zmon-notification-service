package org.zalando.zmon.notifications.oauth;

import org.zalando.zmon.notifications.store.PreSharedKeyStore;

import java.util.Optional;

/**
 * Created by jmussler on 17.11.16.
 */
public class PreSharedTokenInfoService implements TokenInfoService {

    private final PreSharedKeyStore store;

    public PreSharedTokenInfoService(PreSharedKeyStore store) {
        this.store = store;
    }

    @Override
    public Optional<String> lookupUid(String authorizationHeaderValue) {
        if (null == authorizationHeaderValue) {
            return Optional.empty();
        }

        String token = authorizationHeaderValue.replace("Bearer ", "").replace("PreShared ", "");
        if (!store.isKeyValid(token)) {
            return Optional.empty();
        }
        return Optional.of("preshared-token");
    }
}
