package org.zalando.zmon.notifications.pagerduty.webhook.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.zmon.notifications.HttpEventLogger;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.pagerduty.client.PagerDutyClient;
import org.zalando.zmon.notifications.pagerduty.webhook.AlertStore;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Incident;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.MessageType;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.User;

import static org.zalando.zmon.notifications.ZMonEventType.PAGE_RESOLVED;
import static org.zalando.zmon.notifications.pagerduty.webhook.domain.MessageType.RESOLVE;

public class ResolveHandler extends AbstractMessageHandler {
    private final Logger log = LoggerFactory.getLogger(ResolveHandler.class);

    private final HttpEventLogger eventLog;
    private final AlertStore alertStore;

    public ResolveHandler(HttpEventLogger eventLog, AlertStore alertStore, NotificationServiceConfig config, PagerDutyClient client) {
        super(config, client);
        this.eventLog = eventLog;
        this.alertStore = alertStore;
    }

    @Override
    public MessageType getMessageType() {
        return RESOLVE;
    }

    @Override
    String getUsername(final Incident incident) {
        final User assignedToUser = incident.getResolvedByUser();
        return assignedToUser.getEmail();
    }

    @Override
    void doHandleMessage(final int alertId, final String incidentId, final String userName) {
        alertStore.unackAlert(alertId);
        eventLog.log(PAGE_RESOLVED, alertId, incidentId, userName);
        log.info("Alert #{} resolved by {}", alertId, userName);
    }

}
