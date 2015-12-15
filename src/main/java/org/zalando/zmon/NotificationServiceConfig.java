package org.zalando.zmon;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by jmussler on 12/15/15.
 */
@Configuration
@ConfigurationProperties(prefix = "notifications")
public class NotificationServiceConfig {
    private String redisUri;
    private String googleApiKey;
    private String teamServiceUrl;

    public String getTeamServiceUrl() {
        return teamServiceUrl;
    }

    public void setTeamServiceUrl(String teamServiceUrl) {
        this.teamServiceUrl = teamServiceUrl;
    }

    public String getGoogleApiKey() {
        return googleApiKey;
    }

    public void setGoogleApiKey(String googleApiKey) {
        this.googleApiKey = googleApiKey;
    }

    public String getRedisUri() {
        return redisUri;
    }

    public void setRedisUri(String redisUri) {
        this.redisUri = redisUri;
    }
}
