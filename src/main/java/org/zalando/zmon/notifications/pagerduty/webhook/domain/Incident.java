package org.zalando.zmon.notifications.pagerduty.webhook.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Incident {
    private String id;
    private int incidentNumber;
    private Date createdOn;
    private IncidentStatus status;
    private List<Acknowledger> acknowledgers;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIncidentNumber() {
        return incidentNumber;
    }

    public void setIncidentNumber(int incidentNumber) {
        this.incidentNumber = incidentNumber;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public List<Acknowledger> getAcknowledgers() {
        return acknowledgers;
    }

    public void setAcknowledgers(List<Acknowledger> acknowledgers) {
        this.acknowledgers = acknowledgers;
    }
}
