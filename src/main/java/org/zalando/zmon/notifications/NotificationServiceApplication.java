package org.zalando.zmon.notifications;

import com.google.common.base.MoreObjects;
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
import org.zalando.zmon.notifications.store.PreSharedKeyStore;
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

    @Bean
    PreSharedKeyStore getKeyStore(NotificationServiceConfig config) {
        return new PreSharedKeyStore(config.getSharedKeys());
    }

    // request payloads

    public static class DeviceRequestBody {
        public String registration_token;
    }

    public static class SubscriptionRequestBody {
        public int alert_id;
    }

    // defined by google cloud messaging API
    public static class PublishNotificationPart {
        public String title;
        public String body;
        public String icon;

        public PublishNotificationPart() {

        }

        public PublishNotificationPart(String t, String b, String i) {
            icon = i;
            body = b;
            title = t;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("title", title)
                    .add("body", body)
                    .add("icon", icon).toString();
        }
    }

    public static class PublishRequestBody {
        public int alertId;
        public String entityId;
        public PublishNotificationPart notification;

        public PublishNotificationPart getNotification() {
            return notification;
        }

        public void setNotification(PublishNotificationPart notification) {
            this.notification = notification;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("alertId", alertId)
                    .add("notification", notification)
                    .add("entityId", entityId)
                    .toString();
        }
    }

    @Autowired
    TokenInfoService tokenInfoService;

    @Autowired
    PushNotificationService pushNotificationService;

    @Autowired
    NotificationStore notificationStore;

    @Autowired
    PreSharedKeyStore keyStore;

    // registering

    @RequestMapping(value = "/api/v1/device", method = RequestMethod.POST)
    public ResponseEntity<String> registerDevice(@RequestBody DeviceRequestBody body, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {

            if (body.registration_token == null || "".equals(body.registration_token)) {
                return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
            }

            notificationStore.addDeviceForUid(body.registration_token, uid.get());
            LOG.info("Registered device {} for uid {}.", body.registration_token, uid.get());

            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/api/v1/device/{registration_token}", method = RequestMethod.DELETE)
    public ResponseEntity<String> unregisterDevice(@PathVariable(value = "registration_token") String registrationToken, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {

            if (registrationToken == null || "".equals(registrationToken)) {
                return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
            }

            notificationStore.removeDeviceForUid(registrationToken, uid.get());

            LOG.info("Removed device {} for uid {}.", registrationToken, uid.get());
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

    @RequestMapping(value = "/api/v1/subscription/{alert_id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> unregisterSubscription(@PathVariable(value = "alert_id") int alertId, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            notificationStore.removeAlertForUid(alertId, uid.get());
            LOG.info("Removed alert {} for uid {}.", alertId, uid.get());
            return new ResponseEntity<>("", HttpStatus.OK);
        }
        return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
    }

    @RequestMapping(value = "/api/v1/user/subscriptions", method = RequestMethod.GET)
    public ResponseEntity<Collection<Integer>> getRegisteredAlerts(@RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            return new ResponseEntity<>(notificationStore.alertsForUid(uid.get()), HttpStatus.OK);
        }
        return new ResponseEntity<>((Collection<Integer>) null, HttpStatus.UNAUTHORIZED);
    }


    // publishing new alerts

    @RequestMapping(value = "/api/v1/publish", method = RequestMethod.POST)
    public ResponseEntity<String> publishNotification(@RequestBody PublishRequestBody body, @RequestHeader(value = "Authorization", required = false) String oauthHeader) throws IOException {
        boolean authorized = false;

        if (null == oauthHeader) {
            // header not set
        } else if (oauthHeader.startsWith("Bearer")) {
            Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
            if (uid.isPresent()) {
                authorized = true;
            }
        } else if (oauthHeader.startsWith("PreShared")) {
            if (keyStore.isKeyValid(oauthHeader.replace("PreShared ", ""))) {
                authorized = true;
            }
        }

        if (!authorized) {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }

        Collection<String> deviceIds = notificationStore.devicesForAlerts(body.alertId);
        for (String deviceId : deviceIds) {
            pushNotificationService.push(body, deviceId);
        }

        if (deviceIds.size() > 0) {
            LOG.info("Sent alert {} to devices {}.", body.alertId, deviceIds);
        }

        return new ResponseEntity<>("", HttpStatus.OK);
    }

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
