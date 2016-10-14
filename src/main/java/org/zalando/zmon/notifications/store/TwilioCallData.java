package org.zalando.zmon.notifications.store;

import java.util.List;

/**
 * Created by jmussler on 13.10.16.
 */
public class TwilioCallData {
    int alertId;
    List<String> entities;
    String message;
    String voice;
    String incidentId;
    String phone;

    public TwilioCallData(int alertId, List<String> entities, String message, String voice, String incidentId, String phone) {
        this.alertId = alertId;
        this.entities = entities;
        this.message = message;
        this.voice = voice;
        this.incidentId = incidentId;
        this.phone = phone;
    }

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public List<String> getEntities() {
        return entities;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
