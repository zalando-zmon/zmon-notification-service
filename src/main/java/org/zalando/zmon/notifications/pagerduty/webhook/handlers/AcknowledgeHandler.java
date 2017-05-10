package org.zalando.zmon.notifications.pagerduty.webhook.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.zmon.notifications.HttpEventLogger;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.pagerduty.client.PagerDutyClient;
import org.zalando.zmon.notifications.pagerduty.webhook.AlertStore;
import org.zalando.zmon.notifications.pagerduty.webhook.PagerDutyWebHookException;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Acknowledger;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Incident;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.MessageType;

import java.util.List;

import static org.zalando.zmon.notifications.ZMonEventType.PAGE_ACKNOWLEDGED;
import static org.zalando.zmon.notifications.pagerduty.webhook.domain.MessageType.ACKNOWLEDGE;

public class AcknowledgeHandler extends AbstractMessageHandler {
    private final Logger log = LoggerFactory.getLogger(AcknowledgeHandler.class);

    private final HttpEventLogger eventLog;
    private final AlertStore alertStore;

    public AcknowledgeHandler(HttpEventLogger eventLog, AlertStore alertStore, NotificationServiceConfig config, PagerDutyClient client) {
        super(config, client);
        this.eventLog = eventLog;
        this.alertStore = alertStore;
    }

    @Override
    public MessageType getMessageType() {
        return ACKNOWLEDGE;
    }

    @Override
    public String getUsername(final Incident incident) {
        final List<Acknowledger> acknowledgers = incident.getAcknowledgers();

        if(acknowledgers.isEmpty()) {
            throw new PagerDutyWebHookException("Acknowledge without any acknowledgers");
        }
        return acknowledgers.get(0).getObject().getEmail();
    }

    @Override
    public void doHandleMessage(final int alertId, final String incidentId, final String userName) {
        alertStore.ackAlert(alertId);
        eventLog.log(PAGE_ACKNOWLEDGED, alertId, incidentId, userName);
        log.info("User {} acknowledged alert #{}", userName, alertId);
    }

}
