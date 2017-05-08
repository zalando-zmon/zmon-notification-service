package org.zalando.zmon.notifications.pagerduty.client;

import java.util.List;

public interface PagerDutyClient {
    List<Alert> getAlerts(String incidentId);
}
