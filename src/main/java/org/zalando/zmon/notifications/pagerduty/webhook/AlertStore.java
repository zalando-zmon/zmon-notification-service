package org.zalando.zmon.notifications.pagerduty.webhook;

public interface AlertStore {
    void ackAlert(int alertId, String userName);
}
