package org.zalando.zmon.notifications.pagerduty.webhook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum IncidentStatus {
    @JsonProperty("triggered")
    TRIGGERED,
    @JsonProperty("acknowledged")
    ACKNOWLEDGED,
    @JsonProperty("resolved")
    RESOLVED
}
