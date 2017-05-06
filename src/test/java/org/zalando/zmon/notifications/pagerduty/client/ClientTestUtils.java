package org.zalando.zmon.notifications.pagerduty.client;

public final class ClientTestUtils {
    private ClientTestUtils() {}

    public static Alert mockAlert(final String key) {
        final Alert alert = new Alert();
        alert.setAlertKey(key);
        return alert;
    }

}
