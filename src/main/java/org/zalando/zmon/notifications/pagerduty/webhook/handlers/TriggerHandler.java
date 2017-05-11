package org.zalando.zmon.notifications.pagerduty.webhook.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.zmon.notifications.HttpEventLogger;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.pagerduty.client.PagerDutyClient;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Incident;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.MessageType;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.User;

import static org.zalando.zmon.notifications.ZMonEventType.PAGE_TRIGGERED;
import static org.zalando.zmon.notifications.pagerduty.webhook.domain.MessageType.TRIGGER;

public class TriggerHandler extends AbstractMessageHandler {
    private final Logger log = LoggerFactory.getLogger(TriggerHandler.class);

    private final HttpEventLogger eventLog;

    public TriggerHandler(HttpEventLogger eventLog, NotificationServiceConfig config, PagerDutyClient client) {
        super(config, client);
        this.eventLog = eventLog;
    }

    @Override
    public MessageType getMessageType() {
        return TRIGGER;
    }

    @Override
    String getUsername(final Incident incident) {
        final User assignedToUser = incident.getAssignedToUser();
        return assignedToUser.getEmail();
    }

    @Override
    void doHandleMessage(final int alertId, final String incidentId, final String userName) {
        eventLog.log(PAGE_TRIGGERED, alertId, incidentId, userName);
        log.info("Alert #{} assigned to {}", alertId, userName);
    }

}
