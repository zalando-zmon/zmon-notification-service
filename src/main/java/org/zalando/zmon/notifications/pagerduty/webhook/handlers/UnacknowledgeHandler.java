package org.zalando.zmon.notifications.pagerduty.webhook.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.zmon.notifications.HttpEventLogger;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.pagerduty.client.PagerDutyClient;
import org.zalando.zmon.notifications.pagerduty.webhook.AlertStore;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Incident;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.MessageType;

import static org.zalando.zmon.notifications.ZMonEventType.PAGE_UNACKNOWLEDGED;
import static org.zalando.zmon.notifications.pagerduty.webhook.domain.MessageType.UNACKNOWLEDGE;

public class UnacknowledgeHandler extends AbstractMessageHandler {
    public static final String UNACK_USERNAME = "ACK timeout";
    private final Logger log = LoggerFactory.getLogger(UnacknowledgeHandler.class);

    private final HttpEventLogger eventLog;
    private final AlertStore alertStore;

    public UnacknowledgeHandler(NotificationServiceConfig config, HttpEventLogger eventLog, PagerDutyClient client,
                                AlertStore alertStore) {
        super(config, client);
        this.eventLog = eventLog;
        this.alertStore = alertStore;
    }

    @Override
    public MessageType getMessageType() {
        return UNACKNOWLEDGE;
    }

    @Override
    public String getUsername(final Incident incident) {
        return UNACK_USERNAME;
    }

    @Override
    public void doHandleMessage(final int alertId, final String incidentId, final String userName) {
        alertStore.unackAlert(alertId);
        eventLog.log(PAGE_UNACKNOWLEDGED, alertId, incidentId, userName);
        log.info("Unacknowledged alert #{}", alertId);
    }

}
