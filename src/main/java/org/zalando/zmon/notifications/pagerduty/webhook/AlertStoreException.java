package org.zalando.zmon.notifications.pagerduty.webhook;

public class AlertStoreException extends RuntimeException {
    public AlertStoreException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
