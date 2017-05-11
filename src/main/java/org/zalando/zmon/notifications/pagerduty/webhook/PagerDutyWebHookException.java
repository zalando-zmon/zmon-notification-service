package org.zalando.zmon.notifications.pagerduty.webhook;

public class PagerDutyWebHookException extends RuntimeException {
    public PagerDutyWebHookException(final String message) {
        super(message);
    }

    public PagerDutyWebHookException(String format, Object ... args) {
        super(String.format(format, args));
    }
}
