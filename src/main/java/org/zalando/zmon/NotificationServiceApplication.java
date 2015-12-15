package org.zalando.zmon;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@RestController
@SpringBootApplication
public class NotificationServiceApplication {

    @Bean
    JedisPool getRedisPool(NotificationServiceConfig config) throws URISyntaxException {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        return new JedisPool(poolConfig, new URI(config.getRedisUri()));
    }

    @Value("${oauth.tokeninfo.url:}")
    String tokenInfoUrl;

    @Bean
    TokenInfoService getTokenInfoService() {
        return new TokenInfoService(tokenInfoUrl);
    }

    public static class SubscriptionRequestBody {
        public String registration_token;
        public int alert_id;
    }

    public static class PublishRequestBody {
        public JsonNode data;
        public JsonNode notification;
        public int alert_id;
    }

    @Autowired
    JedisPool pool;

    @Value("${oauth.enabled:1}")
    private boolean oauthEnabled;

    @Autowired
    TokenInfoService tokenInfoService;

    @RequestMapping(value="/api/v1/subscription", method = RequestMethod.POST)
    public ResponseEntity<String> registerDeviceToken(@RequestBody SubscriptionRequestBody body, @RequestHeader(value="Authorization", required=false) String oauthHeader) {
        if(oauthEnabled && !tokenInfoService.isValidHeader(oauthHeader)) {
            // intellij complains with null
            return new ResponseEntity<>( "", HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>("", HttpStatus.OK);
    }


    @RequestMapping(value="/api/v1/subscription", method = RequestMethod.DELETE)
    public void unregisterDeviceToken(@RequestBody SubscriptionRequestBody body) {

    }

    @RequestMapping(value="/api/v1/publish", method = RequestMethod.POST)
    public void publishNotification(@RequestBody PublishRequestBody body) {

    }

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
