package org.zalando.zmon.notifications.opsgenie.action;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by mabdelhameed on 21/06/2017.
 */
public enum ActionType {
    @JsonProperty("Create")
    CREATE,
    @JsonProperty("Acknowledge")
    ACKNOWLEDGE,
    @JsonProperty("UnAcknowledge")
    UNACKNOWLEDGE,
    @JsonProperty("Close")
    CLOSE
}
