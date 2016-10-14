package org.zalando.zmon.notifications.store;

/**
 * Created by jmussler on 13.10.16.
 */
public class PendingNotification {
    int alertId;
    int level;
    String phone;
    String entityId;
    String message;
    String incidentId;
    String voice;

    public PendingNotification(int alertId, int level, String incidentId, String phone, String entityId, String message, String voice) {
        this.alertId = alertId;
        this.level = level;
        this.incidentId = incidentId;
        this.phone = phone;
        this.entityId = entityId;
        this.message = message;
        this.voice = voice;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
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
}
