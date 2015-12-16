package org.zalando.zmon.notifications.oauth;

import java.util.Optional;

public class DummyTokenInfoService implements TokenInfoService {

    @Override
    public Optional<String> lookupUid(String authorizationHeaderValue) {
        return Optional.of("one-uid");
    }

    @Override
    public String toString() {
        return "dummy-token-service";
    }
}
