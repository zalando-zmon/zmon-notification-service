package org.zalando.zmon.notifications;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.store.PendingNotification;
import org.zalando.zmon.notifications.store.TwilioCallData;
import org.zalando.zmon.notifications.store.TwilioNotificationStore;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jmussler on 11.10.16.
 */
@RestController
@RequestMapping(path = "/api/v1/twilio")
public class TwilioCallbackAPI {

    final NotificationServiceConfig config;

    final ObjectMapper mapper;

    final TokenInfoService tokenInfoService;

    final TwilioNotificationStore store;

    final HttpEventLogger eventLog;

    final NotificationServiceMetrics metrics;

    private final Logger log = LoggerFactory.getLogger(TwilioCallbackAPI.class);

    @Autowired
    public TwilioCallbackAPI(ObjectMapper objectMapper, TokenInfoService tokenInfoService, NotificationServiceConfig notificationServiceConfig, TwilioNotificationStore twilioNotificationStore, HttpEventLogger eventLog, NotificationServiceMetrics metrics) {
        this.config = notificationServiceConfig;
        this.mapper = objectMapper;
        this.tokenInfoService = tokenInfoService;
        this.store = twilioNotificationStore;
        this.eventLog = eventLog;
        this.metrics = metrics;

        log.info("Twilio mode: dryRun={}", notificationServiceConfig.isDryRun());

        Twilio.init(config.getTwilioUser(), config.getTwilioApiKey());
    }

    // https://github.com/twilio/twilio-java

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<JsonNode> triggerTwilio(@RequestBody TwilioAlert alert, @RequestHeader(value = "Authorization", required = false) String oauthHeader) throws URISyntaxException, IOException {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (!uid.isPresent()) {
            return new ResponseEntity<>((JsonNode) null, HttpStatus.UNAUTHORIZED);
        }

        if (null != alert.getNumbers() && alert.getNumbers().size() <= 0) {
            return new ResponseEntity<>((JsonNode) null, HttpStatus.BAD_REQUEST);
        }

        if (alert.getEventType().equals("ALERT_START")) {
            String incidentId = store.getOrSetIncidentId(alert.getAlertId());
            log.info("Receving ALERT_START: alertId={} entityId={} incidentId={}", alert.getAlertId(), alert.getEntityId(), incidentId);
            store.storeEscalations(alert, incidentId);
            return new ResponseEntity<>((JsonNode) null, HttpStatus.OK);
        }
        else {
            log.info("ALERT_ENDED received: alertId={}", alert.getAlertId());
            if (alert.isAlertChanged()) {

                String incidentId = store.resolveAlert(alert.getAlertId());
                eventLog.log(ZMonEventType.CALL_ALERT_RESOLVED, alert.getAlertId(), incidentId);
            }

            return new ResponseEntity<>((JsonNode) null, HttpStatus.OK);
        }
    }

    @RequestMapping(path="/call", method = RequestMethod.POST, produces = "application/xml")
    public String call(@RequestParam(name = "notification") String id) {
        TwilioCallData data = store.getCallData(id);
        if (null == data) {
            return "";
        }

        log.info("Call request received(phone/mailbox answered): notification={} alertId={} entityIds={}", id, data.getAlertId(), data.getEntities());

        String voice = data.getVoice();
        if (null == voice || "".equals(voice)) {
            voice = "woman";
        }

        eventLog.log(ZMonEventType.CALL_ANSWERED, data.getAlertId(), data.getEntities(), data.getPhone());

        if(data.getEntities().size()>1) {
            return "<Response>\n" +
                    "        <Say voice=\""+ voice +"\">ZMON "+data.getEntities().size()+" entities: " + data.getMessage() + "</Say>\n" +
                    "        <Gather action=\"/api/v1/twilio/response?notification=" + id + "\" method=\"POST\" numDigits=\"1\" timeout=\"10\" finishOnKey=\"#\">\n" +
                    "          <Say voice=\"woman\">" +
                    "               Please enter 1 for ACK" +
                    "                 or 6 for downtime." +
                    "             </Say>\n" +
                    "        </Gather>\n" +
                    "</Response>";
        }

        return "<Response>\n" +
                "        <Say voice=\""+ voice +"\">" + data.getMessage() + "</Say>\n" +
                "        <Gather action=\"/api/v1/twilio/response?notification=" + id + "\" method=\"POST\" numDigits=\"1\" timeout=\"10\" finishOnKey=\"#\">\n" +
                "          <Say voice=\"woman\">Please enter 1 for ACK or 2 for Entity or 6 for downtime.</Say>\n" +
                "        </Gather>\n" +
                "</Response>";
    }

    @RequestMapping(path="/response", method = RequestMethod.POST, produces = "application/xml")
    public ResponseEntity<String> ackNotification(@RequestParam Map<String, String> allParams) {
        log.info("Receiving response for params={}", allParams);
        if(!allParams.containsKey("Digits") || !allParams.containsKey("notification")) {
            return new ResponseEntity<>("<Response><Say>ZMON Response Error</Say></Response>", HttpStatus.BAD_REQUEST);
        }

        String id = allParams.get("notification");
        TwilioCallData data = store.getCallData(id);
        if (null == data) {
            return new ResponseEntity<>("<Response><Say>Notification not found</Say></Response>", HttpStatus.NOT_FOUND);
        }

        String phone = allParams.get("To");
        String digits = allParams.get("Digits");
        String voice = data.getVoice();

        if("1".equals(digits)) {
            log.info("Received ACK for alert: id={} phone={}", id, phone);
            eventLog.log(ZMonEventType.CALL_ALERT_ACK_RECEIVED, data.getAlertId(), phone, data.getIncidentId());
            store.ackAlert(data.getAlertId(), data.getIncidentId(), phone);
            return new ResponseEntity<>("<Response><Say voice=\""+voice+"\">Alert Acknowledged</Say></Response>", HttpStatus.OK);
        }
        else if("2".equals(digits)) {
            log.info("Received ACK for entities: id={} entities={} phone={}", id, data.getEntities(), phone);
            eventLog.log(ZMonEventType.CALL_ENTITY_ACK_RECEIVED, data.getAlertId(), phone, data.getEntities(), data.getIncidentId());
            for(String entity : data.getEntities()) {
                store.ackAlertEntity(data.getAlertId(), entity, data.getIncidentId(), phone);
            }
            return new ResponseEntity<>("<Response><Say voice=\""+voice+"\">Alert Entity Acknowledged</Say></Response>", HttpStatus.OK);
        }
        else if("6".equals(digits)) {
            log.info("Received Downtime for alert");
            return new ResponseEntity<>("<Response><Say voice=\""+voice+"\">Alert Downtime not implemented</Say></Response>", HttpStatus.NOT_IMPLEMENTED);
        }
        else {
            log.info("Wrong digit received!");
            return new ResponseEntity<>("<Response><Say voice=\""+voice+"\">ZMON Response Error</Say></Response>", HttpStatus.BAD_REQUEST);
        }
    }

    public static Map<Integer, Map<String, List<PendingNotification>>> transformResult(List<PendingNotification> results) {
        Map<Integer, Map<String, List<PendingNotification>>> pendingNotifications = new HashMap<>();
        for (PendingNotification p: results) {
            Map<String, List<PendingNotification>> pendingByIncidentId = pendingNotifications.get(p.getAlertId());
            List<PendingNotification> list;
            if (pendingByIncidentId == null) {
                pendingByIncidentId = new HashMap<>();
                pendingNotifications.put(p.getAlertId(), pendingByIncidentId);

                list = new ArrayList<>();
                pendingByIncidentId.put(p.getIncidentId(), list);
            }
            else {
                list = pendingByIncidentId.get(p.getIncidentId());
                if (list == null) {
                    list = new ArrayList<>();
                    pendingByIncidentId.put(p.getIncidentId(), list);
                }
            }
            list.add(p);
        }
        return pendingNotifications;
    }

    @Scheduled(fixedRate = 5000, initialDelay = 60000)
    public void handlePendingCalls() throws URISyntaxException {
        List<PendingNotification> results = store.getPendingNotifications();
        if (null == results) {
            log.error("Error occured receiving pending notifications");
            return;
        }

        if (results.size() > 0) {
            log.info("Received pending notifications: count={}", results.size());
        }

        Map<Integer, Map<String, List<PendingNotification>>> pendingNotifications = transformResult(results);

        if (pendingNotifications.size() > 0) {
            for(Map.Entry<Integer, Map<String, List<PendingNotification>>> byAlertId : pendingNotifications.entrySet()) {
                int alertId = byAlertId.getKey();

                for (Map.Entry<String, List<PendingNotification>> byIncidentId : byAlertId.getValue().entrySet()) {
                    String incidentId = byIncidentId.getKey();

                    if(!store.isIncidentOngoing(alertId, incidentId)) {
                        log.info("Received old incidentId, skipping notify: alertId={} incidentId={}", alertId, incidentId);
                        continue;
                    }

                    List<PendingNotification> filtered = new ArrayList<>();
                    for (PendingNotification p : byIncidentId.getValue()) {
                        if (!store.isAck(p.getAlertId(), incidentId, p.getEntityId())) {
                            filtered.add(p);
                        }
                    }

                    if (filtered.size() > 0) {
                        int level = filtered.get(0).getLevel();
                        String phone = filtered.get(0).getPhone();

                        // we call lowest escalation here, we will assume there is always one higher escalation at next poll due to delay for at least one entity
                        for (PendingNotification p : filtered) {
                            if (p.getLevel() < level) {
                                level = p.getLevel();
                                phone = p.getPhone();
                            }
                        }

                        if (store.lockAlert(alertId)) {
                            List<String> entities = filtered.stream().map(x->x.getEntityId()).collect(Collectors.toList());
                            log.info("Calling ... : alertId={} level={} phone={} entities={}", alertId, level, phone, entities);
                            TwilioCallData data = new TwilioCallData(alertId, entities, filtered.get(0).getMessage(), filtered.get(0).getVoice(), incidentId, phone);
                            String uuid = store.storeCallData(data);

                            if(!config.isDryRun()) {
                                Call call = Call.creator(config.getTwilioUser(),
                                        new PhoneNumber(phone),
                                        new PhoneNumber(config.getTwilioPhoneNumber()),
                                        new URI(config.getDomain() + "/api/v1/twilio/call?notification=" + uuid)).create();
                            }
                            else {
                                log.info("DRY RUN CALL: {}", "/api/v1/twilio/call?notification=" + uuid);
                            }
                        }
                    } else {
                        log.info("All entities are ACK, skipping call: alertId={} incidentId={}", alertId, incidentId);
                    }
                }
            }
        }
    }
}
