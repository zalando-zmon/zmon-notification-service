package org.zalando.zmon.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;
import org.zalando.zmon.notifications.api.*;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.oauth.ChainedTokenInfo;
import org.zalando.zmon.notifications.oauth.OAuthTokenInfoService;
import org.zalando.zmon.notifications.oauth.PreSharedTokenInfoService;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.push.GooglePushNotificationService;
import org.zalando.zmon.notifications.push.PushNotificationService;
import org.zalando.zmon.notifications.store.NotificationStore;
import org.zalando.zmon.notifications.store.PreSharedKeyStore;
import org.zalando.zmon.notifications.store.RedisNotificationStore;
import org.zalando.zmon.notifications.store.TwilioNotificationStore;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RestController
@EnableScheduling
@EnableConfigurationProperties
@SpringBootApplication
public class NotificationServiceApplication {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationServiceApplication.class);

    @Autowired
    ObjectMapper mapper;

    @Autowired
    NotificationServiceConfig config;

    @Autowired
    EscalationConfigSource escalationConfigSource;

    @Bean
    TokenInfoService getTokenInfoService(NotificationServiceConfig config, PreSharedKeyStore preSharedKeyStore) {
        ChainedTokenInfo info = new ChainedTokenInfo(new PreSharedTokenInfoService(preSharedKeyStore),
                                                     new OAuthTokenInfoService(config.getOauthInfoServiceUrl()));
        return info;
    }

    @Bean
    PushNotificationService getPushNotificationService() {
        return new GooglePushNotificationService(config.getGooglePushServiceUrl(), config.getGooglePushServiceApiKey());
    }

    @Bean
    NotificationStore getNotificationStore() throws URISyntaxException {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(poolConfig, new URI(config.getRedisUri()));
        return new RedisNotificationStore(jedisPool, mapper);
    }

    @Bean
    TwilioNotificationStore getTwilioNotificationStore(NotificationServiceConfig config) throws URISyntaxException {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(poolConfig, new URI(config.getRedisUri()));
        return new TwilioNotificationStore(jedisPool, escalationConfigSource, mapper);
    }

    @Bean
    PreSharedKeyStore getKeyStore(NotificationServiceConfig config) {
        return new PreSharedKeyStore(config.getSharedKeys());
    }

    @Autowired
    TokenInfoService tokenInfoService;

    @Autowired
    PushNotificationService pushNotificationService;

    @Autowired
    NotificationStore notificationStore;

    @Autowired
    PreSharedKeyStore keyStore;

    @RequestMapping(value = "/api/v1/device", method = RequestMethod.POST)
    public ResponseEntity<String> registerDevice(@RequestBody DeviceRequestBody body, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {

            if (body.registrationToken == null || "".equals(body.registrationToken)) {
                return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
            }

            notificationStore.addDeviceForUid(body.registrationToken, uid.get());
            LOG.info("Registered device {} for uid {}.", body.registrationToken.substring(0, 5), uid.get());

            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/api/v1/users/{name}/devices", method = RequestMethod.POST)
    public ResponseEntity<String> registerDeviceToUser(@PathVariable("name") String userId, @RequestBody DeviceRequestBody body, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            if (body.registrationToken == null || "".equals(body.registrationToken)) {
                return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
            }

            notificationStore.addDeviceForUid(body.registrationToken, userId);
            LOG.info("Registered device {} for uid {}.", body.registrationToken.substring(0, 5), userId);

            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/api/v1/users/{name}/devices", method = RequestMethod.GET)
    public ResponseEntity<Collection<String>> getRegisteredDevices(@PathVariable("name") String userId, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            return new ResponseEntity<>(notificationStore.devicesForUid(userId), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/api/v1/device/{registration_token}", method = RequestMethod.DELETE)
    public ResponseEntity<String> unregisterDevice(@PathVariable("registration_token") String registrationToken, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {

            if (registrationToken == null || "".equals(registrationToken)) {
                return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
            }

            notificationStore.removeDeviceForUid(registrationToken, uid.get());

            LOG.info("Removed device {} for uid {}.", registrationToken.substring(0, 5), uid.get());
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/api/v1/users/{name}/teams", method = RequestMethod.GET)
    public ResponseEntity<Collection<String>> getSubscribedTeams(@PathVariable("name") String userId, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            Collection<String> teams = notificationStore.teamsForUid(userId);
            return new ResponseEntity<>(teams, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/api/v1/users/{name}/teams", method = RequestMethod.POST)
    public ResponseEntity<String> subscribeToTeam(@PathVariable("name") String userId, @RequestBody SubscriptionRequestBody body, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            notificationStore.addTeamToUid(body.team, userId);
            LOG.info("Registered team {} for uid {}.", body.team, userId);
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/api/v1/users/{name}/priority", method = RequestMethod.GET)
    public ResponseEntity<Integer> getPriority(@PathVariable("name") String userId, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            int priority = notificationStore.getPriority(userId);
            return new ResponseEntity<>(priority, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/api/v1/users/{name}/priority", method = RequestMethod.POST)
    public ResponseEntity<String> setPriority(@PathVariable("name") String userId, @RequestBody PriorityBody body, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            notificationStore.setPriority(body.priority, userId);
            LOG.info("Registered team {} for uid {}.", body.priority, userId);
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/api/v1/users/{name}/alerts", method = RequestMethod.POST)
    public ResponseEntity<String> subscribeToAlert(@PathVariable("name") String userId, @RequestBody SubscriptionRequestBody body, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            notificationStore.addAlertForUid(body.alert_id, userId);
            LOG.info("Registered Alert {} for uid {}.", body.alert_id, userId);
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/api/v1/users/{name}/alerts", method = RequestMethod.GET)
    public ResponseEntity<Collection<Integer>> getSubscribedAlerts(@PathVariable("name") String userId, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            return new ResponseEntity<>(notificationStore.alertsForUid(userId), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @RequestMapping(value = "/api/v1/users/{name}/teams", method = RequestMethod.DELETE)
    public ResponseEntity<String> subscribeToTeam(@PathVariable("name") String userId, @RequestParam(name="team") String team, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            notificationStore.removeTeamFromUid(team, userId);
            LOG.info("Removed team {} for uid {}.", team, userId);
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }
    }

    @RequestMapping(value = "/api/v1/users/{name}/alerts", method = RequestMethod.DELETE)
    public ResponseEntity<String> removetAlertSubscription(@PathVariable("name") String userId, @RequestParam(name="alertId") int alertId, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (uid.isPresent()) {
            notificationStore.removeAlertForUid(alertId, userId);
            LOG.info("Removed alert {} for uid {}.", alertId, userId);
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
    public ResponseEntity<String> unregisterSubscription(@PathVariable("alert_id") int alertId, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
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

    private final ThreadPoolExecutor pushExecutor = new ThreadPoolExecutor(5, 10, 10, TimeUnit.MINUTES, new ArrayBlockingQueue<>(5000));

    private class PushTaskExecutable implements Runnable {

        private PublishRequestBody body;

        PushTaskExecutable(PublishRequestBody body) {
            this.body = body;
        }

        @Override
        public void run() {
            // prepend domain to click action
            if (null == body.notification.click_action) {
                body.notification.click_action = config.getZmonUrl();
            }
            else if(!body.notification.click_action.startsWith("https://")) {
                body.notification.click_action = config.getZmonUrl() + body.notification.click_action;
            }

            Collection<String> deviceIds = notificationStore.devicesForAlerts(body.alertId, body.team, body.priority);
            for (String deviceId : deviceIds) {
                try {
                    pushNotificationService.push(body, deviceId);
                }
                catch(IOException ex) {

                }
            }

            if (deviceIds.size() > 0) {
                LOG.info("Sent alert {} to {} devices.", body.alertId, deviceIds.size());
            }
        }
    }

    // publishing new alerts
    @RequestMapping(value = "/api/v1/publish", method = RequestMethod.POST)
    public ResponseEntity<String> publishNotification(@RequestBody PublishRequestBody body, @RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        if (!tokenInfoService.lookupUid(oauthHeader).isPresent()) {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }

        pushExecutor.execute(new PushTaskExecutable(body));

        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @RequestMapping(value = "/api/v1/publish-global", method = RequestMethod.POST)
    public ResponseEntity<String> publishGlobalNotification(@RequestBody PublishNotificationPart body, @RequestHeader(value = "Authorization", required = false) String oauthHeader) throws IOException {
        if (!tokenInfoService.lookupUid(oauthHeader).isPresent()) {
            return new ResponseEntity<>("", HttpStatus.UNAUTHORIZED);
        }

        for (String deviceId : notificationStore.getAllDeviceIds()) {
            // pushNotificationService.push(body, deviceId);
        }

        return new ResponseEntity<>("", HttpStatus.OK);
    }

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
