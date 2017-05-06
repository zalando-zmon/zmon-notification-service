package org.zalando.zmon.notifications.pagerduty.webhook.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Acknowledger {
    private Date at;
    private User object;

    public Date getAt() {
        return at;
    }

    public void setAt(Date at) {
        this.at = at;
    }

    public User getObject() {
        return object;
    }

    public void setObject(User object) {
        this.object = object;
    }
}
