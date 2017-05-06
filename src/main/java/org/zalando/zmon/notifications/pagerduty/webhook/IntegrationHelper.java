package org.zalando.zmon.notifications.pagerduty.webhook;

import com.google.common.base.Strings;

public final class IntegrationHelper {
    private IntegrationHelper() {}

    public static int alertIdFromAlertKey(final String alertKey) {
        if(Strings.isNullOrEmpty(alertKey)) {
            throw new IllegalArgumentException("alertKey must be a valid, non-empty, string");
        }
        if(!alertKey.startsWith("ZMON-")) {
            throw new IllegalArgumentException("alertKey must have the prefix `ZMON-`");
        }
        return Integer.parseInt(alertKey.replace("ZMON-", ""));
    }
}
