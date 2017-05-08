package org.zalando.zmon.notifications.pagerduty.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zalando.zmon.notifications.HttpEventLogger;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.pagerduty.client.Alert;
import org.zalando.zmon.notifications.pagerduty.client.PagerDutyClient;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.*;

import java.util.List;
import java.util.Optional;

import static org.zalando.zmon.notifications.ZMonEventType.PAGE_ACKNOWLEDGED;
import static org.zalando.zmon.notifications.pagerduty.webhook.IntegrationHelper.alertIdFromAlertKey;

@RestController
@RequestMapping(path = "/api/v1/pagerduty")
public class PagerDutyWebHookController {
    private final Logger log = LoggerFactory.getLogger(PagerDutyWebHookController.class);

    private final NotificationServiceConfig config;
    private final TokenInfoService tokenInfoService;
    private final HttpEventLogger eventLog;
    private final PagerDutyClient client;
    private final AlertStore alertStore;

    @Autowired
    public PagerDutyWebHookController(final PagerDutyClient client, final TokenInfoService tokenInfoService,
                                      final NotificationServiceConfig notificationServiceConfig, final HttpEventLogger eventLog,
                                      final AlertStore alertStore) {
        this.config = notificationServiceConfig;
        this.tokenInfoService = tokenInfoService;
        this.eventLog = eventLog;
        this.client = client;
        this.alertStore = alertStore;
        log.info("PagerDuty mode: dryRun={}", notificationServiceConfig.isDryRun());
    }

    @RequestMapping(path="/webhook", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Void> ackNotification(@RequestParam(required = false) String psk, @RequestBody Callback callback) {
        log.debug("Received PagerDuty Web Hook call");
        Optional<String> uid = tokenInfoService.lookupUid(psk);
        if (!uid.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        for (final Message message: callback.getMessages()) {
            if(!handleCallbackMessage(message)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        return ResponseEntity.ok().build();
    }

    private boolean handleCallbackMessage(final Message message) {
        final MessageType messageType = message.getType();
        switch(messageType) {
            case ACKNOWLEDGE: {
                return handleAcknowledge(message.getData());
            }
            default:
                log.info("No handler for PagerDuty message type {}", messageType);
        }
        return true;
    }

    private boolean handleAcknowledge(final Data data) {
        final Incident incident = data.getIncident();
        final String incidentId = incident.getId();
        final List<Acknowledger> acknowledgers = incident.getAcknowledgers();

        if(acknowledgers.isEmpty()) {
            log.error("Acknowledge without any acknowledgers");
            return false;
        }
        final String userName = acknowledgers.get(0).getObject().getEmail();

        log.info("Received ACK for incident: id={} from user={}", incidentId, userName);
        final List<Alert> alerts = client.getAlerts(incidentId);
        if (alerts != null && !alerts.isEmpty()) {
            final Alert firstAlert = alerts.get(0);
            final int alertId = alertIdFromAlertKey(firstAlert.getAlertKey());
            if(!config.isDryRun()) {
                alertStore.ackAlert(alertId, userName);
                eventLog.log(PAGE_ACKNOWLEDGED, alertId, userName);
                log.info("Acknowledged alert #{}", alertId);
            } else {
                log.info("Running in Dry-Run mode. No changes applied");
            }
        } else {
            log.error("Couldn't obtain PagerDuty Alerts for Incident {}", incidentId);
            return false;
        }
        return true;
    }

}
