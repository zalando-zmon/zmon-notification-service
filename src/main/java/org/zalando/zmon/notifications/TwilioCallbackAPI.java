package org.zalando.zmon.notifications;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.store.TwilioNotificationStore;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

/**
 * Created by jmussler on 11.10.16.
 */
@RestController
@RequestMapping(path = "/api/v1/twilio")
public class TwilioCallbackAPI {

    @Autowired
    NotificationServiceConfig config;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    TokenInfoService tokenInfoService;

    @Autowired
    TwilioNotificationStore store;

    private final Logger log = LoggerFactory.getLogger(TwilioCallbackAPI.class);

    @Bean
    TwilioNotificationStore getTwilioNotificationStore() throws URISyntaxException {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(poolConfig, new URI(config.getRedisUri()));
        return new TwilioNotificationStore(jedisPool, mapper);
    }

    // https://github.com/twilio/twilio-java

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<JsonNode> triggerTwilio(@RequestBody TwilioAlert alert, @RequestHeader(value = "Authorization", required = false) String oauthHeader) throws URISyntaxException, IOException {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (!uid.isPresent()) {
            return new ResponseEntity<>((JsonNode) null, HttpStatus.UNAUTHORIZED);
        }

        if (alert.getNumbers().size() <= 0) {
            return new ResponseEntity<>((JsonNode) null, HttpStatus.BAD_REQUEST);
        }

        log.info("Storing alert={} and triggering call to first number", alert);

        String uuid = store.storeAlert(alert);
        if (null == uuid) {
            return new ResponseEntity<>((JsonNode) null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Twilio.init(config.getTwilioUser(), config.getTwilioApiKey());
        Call call = Call.creator(config.getTwilioUser(), new PhoneNumber(alert.getNumbers().get(0)), new PhoneNumber(config.getTwilioPhoneNumber()), new URI(config.getDomain() + "/api/v1/twilio/call?notification=" + uuid)).create();

        return new ResponseEntity<>((JsonNode) null, HttpStatus.OK);
    }

    @RequestMapping(path="/call", method = RequestMethod.POST, produces = "application/xml")
    public String call(@RequestParam(name = "notification") String id) {
        log.info("Start call request for id={}", id);
        TwilioAlert alert = store.getAlert(id);

        return "<Response>\n" +
                "        <Say voice=\"woman\">" + alert.getName() + "</Say>\n" +
                "        <Gather action=\"/api/v1/twilio/response?notification=" + id + "\" method=\"POST\" numDigits=\"1\" timeout=\"10\" finishOnKey=\"#\">\n" +
                "          <Say voice=\"woman\">Please enter 1 for ACK and 6 for Resolve.</Say>\n" +
                "        </Gather>\n" +
                "</Response>";
    }

    @RequestMapping(path="/response", method = RequestMethod.POST, produces = "application/xml")
    public String ackNotification(@RequestParam Map<String, String> allParams) {
        log.info("Receiving Twilio response for params={}", allParams);
        if(!allParams.containsKey("Digits")) {
            return "<Response><Say>ZMON Response Error</Say></Response>";
        }

        String digits = allParams.get("Digits");
        if("1".equals(digits)) {
            log.info("Received ACK for alert");
            return "<Response><Say>Alert Acknowledged</Say></Response>";
        }
        else if("6".equals(digits)) {
            log.info("Received RESOLVED for alert");
            return "<Response><Say>Alert Resolved</Say></Response>";
        }
        else {
            return "<Response><Say>ZMON Response Error</Say></Response>";
        }
    }
}
