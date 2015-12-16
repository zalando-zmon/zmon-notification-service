package org.zalando.zmon.notifications;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Created by jmussler on 12/15/15.
 */
@Configuration
@ConfigurationProperties(prefix = "notifications")
public class NotificationServiceConfig {
    private String oauthInfoServiceUrl;

    private String googleUrl;
    private String googleApiKey;

    private String redisUri;

    public Map<String, Long> sharedKeys;

    public Map<String, Long> getSharedKeys() {
        return sharedKeys;
    }

    public void setSharedKeys(Map<String, Long> sharedKeys) {
        this.sharedKeys = sharedKeys;
    }

    public String getOauthInfoServiceUrl() {
        return oauthInfoServiceUrl;
    }

    public void setOauthInfoServiceUrl(String teamServiceUrl) {
        this.oauthInfoServiceUrl = teamServiceUrl;
    }


    public String getGooglePushServiceUrl() {
        return googleUrl;
    }

    public void setGooglePushServiceUrl(String googleUrl) {
        this.googleUrl = googleUrl;
    }


    public String getGooglePushServiceApiKey() {
        return googleApiKey;
    }

    public void setGooglePushServiceApiKey(String googleApiKey) {
        this.googleApiKey = googleApiKey;
    }


    public String getRedisUri() {
        return redisUri;
    }

    public void setRedisUri(String redisUri) {
        this.redisUri = redisUri;
    }
}
