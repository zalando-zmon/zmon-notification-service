package org.zalando.zmon.notifications;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Notification;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.store.TwilioNotificationStore;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private final Logger log = LoggerFactory.getLogger(TwilioCallbackAPI.class);

    @Autowired
    public TwilioCallbackAPI(ObjectMapper objectMapper, TokenInfoService tokenInfoService, NotificationServiceConfig notificationServiceConfig, TwilioNotificationStore twilioNotificationStore) {
        this.config = notificationServiceConfig;
        this.mapper = objectMapper;
        this.tokenInfoService = tokenInfoService;
        this.store = twilioNotificationStore;

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
            log.info("Storing alertId={} entityId={}", alert.getAlertId(), alert.getEntityId());
            String uuid = store.storeAlert(alert);
            if (null == uuid) {
                return new ResponseEntity<>((JsonNode) null, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            boolean isAck = store.isAck(alert.getAlertId(), alert.getEntityId());
            if(isAck) {
                log.info("Alert start received, but is already ACK: alertId={} entityId={}", alert.getAlertId(), alert.getEntityId());

                // no escalation stored at this point due to matching ACK
                return new ResponseEntity<>((JsonNode) null, HttpStatus.OK);
            }

            // store escalation independent of lock, lock maybe from other entity and we wait notify period to trigger call for another entity
            store.storeEscalations(alert, uuid);

            boolean lock = store.lockAlert(alert.getAlertId(), alert.getEntityId());
            if(!lock) {
                log.info("Notification skipped, notification in progress: alertId={} entity={}", alert.getAlertId(), alert.getEntityId());
                return new ResponseEntity<>((JsonNode) null, HttpStatus.OK);
            }

            Call call = Call.creator(config.getTwilioUser(), new PhoneNumber(alert.getNumbers().get(0)), new PhoneNumber(config.getTwilioPhoneNumber()), new URI(config.getDomain() + "/api/v1/twilio/call?notification=" + uuid)).create();

            return new ResponseEntity<>((JsonNode) null, HttpStatus.OK);
        }
        else {
            log.info("ALERT_ENDED received: alertId={}", alert.getAlertId());
            if (alert.isAlertChanged()) {
                store.resolveAlert(alert.getAlertId());
            }

            return new ResponseEntity<>((JsonNode) null, HttpStatus.OK);
        }
    }

    @RequestMapping(path="/call", method = RequestMethod.POST, produces = "application/xml")
    public String call(@RequestParam(name = "notification") String id) {
        TwilioAlert alert = store.getAlert(id);
        if (null == alert) {
            return "";
        }

        log.info("Start call request: notification={} alertId={} entityId={}", id, alert.getAlertId(), alert.getEntityId());

        String voice = alert.getVoice();
        if (null == voice || "".equals(voice)) {
            voice = "woman";
        }

        return "<Response>\n" +
                "        <Say voice=\""+ voice +"\">" + alert.getMessage() + "</Say>\n" +
                "        <Gather action=\"/api/v1/twilio/response?notification=" + id + "\" method=\"POST\" numDigits=\"1\" timeout=\"10\" finishOnKey=\"#\">\n" +
                "          <Say voice=\"woman\">Please enter 1 for ACK or 2 for Entity or 6 resolve.</Say>\n" +
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
        TwilioAlert alert = store.getAlert(id);
        if (null == alert) {
            return new ResponseEntity<>("<Response><Say>Notification not found</Say></Response>", HttpStatus.NOT_FOUND);
        }

        String phone = allParams.get("To");
        String digits = allParams.get("Digits");
        String voice = alert.getVoice();

        if("1".equals(digits)) {
            log.info("Received ACK for alert: id={} phone={}", id, phone);
            store.ackAlert(alert.getAlertId(), phone);
            return new ResponseEntity<>("<Response><Say voice=\""+voice+"\">Alert Acknowledged</Say></Response>", HttpStatus.OK);
        }
        else if("2".equals(digits)) {
            log.info("Received ACK for entity: id={} entity={} phone={}", id, alert.getEntityId(), phone);
            store.ackAlertEntity(alert.getAlertId(), alert.getEntityId(), phone);
            return new ResponseEntity<>("<Response><Say voice=\""+voice+"\">Alert Entity Acknowledged</Say></Response>", HttpStatus.OK);
        }
        else if("6".equals(digits)) {
            log.info("Received RESOLVED for alert");
            store.resolveAlert(alert.getAlertId());
            return new ResponseEntity<>("<Response><Say voice=\""+voice+"\">Alert Resolved</Say></Response>", HttpStatus.OK);
        }
        else {
            log.info("Wrong digit received!");
            return new ResponseEntity<>("<Response><Say voice=\""+voice+"\">ZMON Response Error</Say></Response>", HttpStatus.BAD_REQUEST);
        }
    }

    @Scheduled(fixedRate = 15000, initialDelay = 60000)
    public void handlePendingCalls() throws URISyntaxException {
        List<String> results = store.getPendingNotifications();
        if (null == results) {
            log.error("Error occured receiving pending notifications");
        }

        if (results.size() > 0) {
            log.info("Received pending notifications: count={}", results.size());
        }

        for (int i = 0; i < results.size(); i++) {
            String[] data = results.get(i).split(":");
            String number = data[0];
            String uuid = data[1];

            TwilioAlert alert = store.getAlert(uuid);
            if (store.isAck(alert.getAlertId(), alert.getEntityId())) {
                log.info("Alert is in state ACK skipping call: alertId={} entityId={}", alert.getAlertId(), alert.getEntityId());
                continue;
            }

            log.info("Escalation call: notification={} phone={} alertId={}", uuid, number, alert.getAlertId());

            Call call = Call.creator(config.getTwilioUser(), new PhoneNumber(number), new PhoneNumber(config.getTwilioPhoneNumber()), new URI(config.getDomain() + "/api/v1/twilio/call?notification=" + uuid)).create();
        }
    }
}
