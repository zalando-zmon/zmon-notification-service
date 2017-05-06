package org.zalando.zmon.notifications.pagerduty.webhook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

// https://v2.developer.pagerduty.com/docs/webhooks-overview#webhook-types
public enum MessageType {
    @JsonProperty("incident.trigger")
    TRIGGER,
    @JsonProperty("incident.acknowledge")
    ACKNOWLEDGE,
    @JsonProperty("incident.unacknowledge")
    UNACKNOWLEDGE,
    @JsonProperty("incident.resolve")
    RESOLVE,
    @JsonProperty("incident.assign")
    ASSIGN,
    @JsonProperty("incident.escalate")
    ESCALATE,
    @JsonProperty("incident.delegate")
    DELEGATE
}
