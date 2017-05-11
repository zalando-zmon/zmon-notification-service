package org.zalando.zmon.notifications.pagerduty.webhook.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.pagerduty.client.PagerDutyClient;
import org.zalando.zmon.notifications.pagerduty.webhook.MessageHandler;
import org.zalando.zmon.notifications.pagerduty.webhook.PagerDutyWebHookException;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Incident;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Message;

import java.util.Optional;

import static org.zalando.zmon.notifications.pagerduty.webhook.IntegrationHelper.getAlertId;

public abstract class AbstractMessageHandler implements MessageHandler {
    private final Logger log = LoggerFactory.getLogger(AbstractMessageHandler.class);

    private final NotificationServiceConfig config;
    private final PagerDutyClient client;

    AbstractMessageHandler(final NotificationServiceConfig config, final PagerDutyClient client) {
        this.config = config;
        this.client = client;
    }

    @Override
    public void handleMessage(final Message message) {
        final Incident incident = message.getData().getIncident();
        final String incidentId = incident.getId();
        log.info("Handling {} for Incident #{}", incidentId, message.getType());
        final Optional<Integer> alertId = getAlertId(client, incidentId);
        if (alertId.isPresent()) {
            if(!config.isDryRun()) {
                final Integer id = alertId.get();
                final String userName = getUsername(incident);
                doHandleMessage(id, incidentId, userName);
            } else {
                log.info("Running in Dry-Run mode. No changes applied");
            }
        } else {
            throw new PagerDutyWebHookException("Couldn't obtain alerts for incident id=%s", incidentId);
        }

    }

    abstract String getUsername(final Incident incident);

    abstract void doHandleMessage(final int alertId, final String incidentId, final String userName);
}
