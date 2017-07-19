package org.zalando.zmon.notifications.opsgenie.action;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;

/**
 * Created by mabdelhameed on 21/06/2017.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlertAction {
    private ActionType action;
    private String integrationName;
    private Alert alert;

    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public String getIntegrationName() {
        return integrationName;
    }

    public void setIntegrationName(String integrationName) {
        this.integrationName = integrationName;
    }

    public Alert getAlert() {
        return alert;
    }

    public void setAlert(Alert alert) {
        this.alert = alert;
    }

    public int getAlertId() {
        String alias = alert.getAlias();

        if(Strings.isNullOrEmpty(alias)) {
            throw new IllegalArgumentException("alertKey must be a valid, non-empty, string");
        }
        if(!alias.startsWith("ZMON-")) {
            throw new IllegalArgumentException("alertKey must have the prefix `ZMON-`");
        }

        return Integer.parseInt(alias.split("-")[1]);
    }

    @Override
    public String toString() {
        return "AlertAction{" +
                "action=" + action +
                ", integrationName='" + integrationName + '\'' +
                ", alert=" + alert.toString() +
                '}';
    }
}
