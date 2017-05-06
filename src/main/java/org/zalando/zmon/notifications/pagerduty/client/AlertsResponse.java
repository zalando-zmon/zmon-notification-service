package org.zalando.zmon.notifications.pagerduty.client;

import com.google.common.collect.ImmutableList;

import java.util.List;

class AlertsResponse {
    private List<Alert> alerts = ImmutableList.of();

    List<Alert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<Alert> alerts) {
        this.alerts = alerts;
    }
}
