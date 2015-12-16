package org.zalando.zmon.notifications.oauth;

import java.util.Optional;

public interface TokenInfoService {
    Optional<String> lookupUid(String authorizationHeaderValue);
}
