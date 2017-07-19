package org.zalando.zmon.notifications.opsgenie.action;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mabdelhameed on 21/06/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Alert {
    private String alias;
    private String tinyId;
    private String username = "UNKNOWN";
    private List<String> recipients = new ArrayList<String>();

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }

    public String getTinyId() { return tinyId; }

    public void setTinyId(String tinyId) { this.tinyId = tinyId; }
}
