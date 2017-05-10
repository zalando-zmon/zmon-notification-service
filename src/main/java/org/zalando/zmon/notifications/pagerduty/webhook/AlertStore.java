package org.zalando.zmon.notifications.pagerduty.webhook;

public interface AlertStore {
    void ackAlert(int alertId);

    void unackAlert(int alertId);
}
