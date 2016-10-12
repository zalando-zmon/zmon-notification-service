package org.zalando.zmon.notifications;

import java.util.List;

/**
 * Created by jmussler on 11.10.16.
 */
public class TwilioAlert {
    private String message;
    private String responsibleTeam;
    private String entityId;
    private String voice;
    private String eventType;
    private boolean alertChanged;
    private int alertId;
    private List<String> numbers;

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getResponsibleTeam() {
        return responsibleTeam;
    }

    public void setResponsibleTeam(String responsibleTeam) {
        this.responsibleTeam = responsibleTeam;
    }

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public List<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<String> numbers) {
        this.numbers = numbers;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public boolean isAlertChanged() {
        return alertChanged;
    }

    public void setAlertChanged(boolean alertChanged) {
        this.alertChanged = alertChanged;
    }
}
