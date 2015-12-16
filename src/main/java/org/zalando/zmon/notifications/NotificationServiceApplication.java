package org.zalando.zmon.notifications;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zalando.zmon.notifications.oauth.OAuthTokenInfoService;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.push.GooglePushNotificationService;
import org.zalando.zmon.notifications.push.PushNotificationService;
import org.zalando.zmon.notifications.store.NotificationStore;
import org.zalando.zmon.notifications.store.RedisNotificationStore;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Optional;

@RestController
@EnableConfigurationProperties
@SpringBootApplication
public class NotificationServiceApplication {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationServiceApplication.class);

    @Autowired
    NotificationServiceConfig config;

    @Bean
    TokenInfoService getTokenInfoService() {
        return new OAuthTokenInfoService(config.getOauthInfoServiceUrl());
    }

    @Bean
    PushNotificationService getPushNotificationService() {
        return new GooglePushNotificationService(config.getGooglePushServiceUrl(), config.getGooglePushServiceApiKey());
    }

    @Bean
    NotificationStore getNotificationStore() throws URISyntaxException {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(poolConfig, new URI(config.getRedisUri()));
        return new RedisNotificationStore(jedisPool);
    }

    // request payloads

    public static class DeviceRequestBody {
        public String registration_token;
    }

    public static class SubscriptionRequestBody {
        public int alert_id;
    }

    public static class PublishRequestBody {
        public int alert_id;
        public JsonNode data;
        public JsonNode notification;
    }

    @Autowired
    TokenInfoService tokenInfoService;

    @Autowired
    PushNotificationService pushNotificationService;

    @Autowired
    NotificationStore notificationStore;

    // registering

    @RequestMapping(value = "/api/v1/device", method = RequestMethod.POST)
    public ResponseEntity<String> registerDevice(@RequestBody DeviceRequestBody body, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            notificationStore.addDeviceForUid(body.registration_token, uid.get());
            LOG.info("Registered device {} for uid {}.", body.registration_token, uid.get());
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/api/v1/subscription", method = RequestMethod.POST)
    public ResponseEntity<String> registerSubscription(@RequestBody SubscriptionRequestBody body, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            notificationStore.addAlertForUid(body.alert_id, uid.get());
            LOG.info("Registered alert {} for uid {}.", body.alert_id, uid.get());
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }


    // publishing new alerts

    @RequestMapping(value = "/api/v1/publish", method = RequestMethod.POST)
    public void publishNotification(@RequestBody PublishRequestBody body) throws IOException {
        Collection<String> deviceIds = notificationStore.devicesForAlerts(body.alert_id);
        for (String deviceId : deviceIds) {
            pushNotificationService.push(body, deviceId);
        }

        LOG.info("Sent alert {} to devices {}.", body.alert_id, deviceIds);
    }

    // Unregistering

    @RequestMapping(value = "/api/v1/device", method = RequestMethod.DELETE)
    public ResponseEntity<String> unregisterDevice(@RequestBody DeviceRequestBody body, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            notificationStore.removeDeviceForUid(body.registration_token, uid.get());
            LOG.info("Removed device {} for uid {}.", body.registration_token, uid.get());
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/api/v1/subscription", method = RequestMethod.DELETE)
    public ResponseEntity<String> unregisterSubscription(@RequestBody SubscriptionRequestBody body, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            notificationStore.removeAlertForUid(body.alert_id, uid.get());
            LOG.info("Removed alert {} for uid {}.", body.alert_id, uid.get());
            return new ResponseEntity<>("", HttpStatus.OK);
        }
        return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
    }

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
