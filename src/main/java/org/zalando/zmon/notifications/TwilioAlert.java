package org.zalando.zmon.notifications;

import java.util.List;

/**
 * Created by jmussler on 11.10.16.
 */
public class TwilioAlert {
    private String name;
    private String responsibleTeam;
    private int alertId;
    private List<String> numbers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public String toString() {
        return "TwilioAlert {" +
                "name='" + name + '\'' +
                ", responsibleTeam='" + responsibleTeam + '\'' +
                ", alertId=" + alertId +
                ", numbers=" + numbers +
                '}';
    }
}
