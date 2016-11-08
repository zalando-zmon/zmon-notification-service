package org.zalando.zmon.notifications;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
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

    private String twilioApiKey;
    private String twilioUser;
    private String twilioPhoneNumber;
    private String domain;

    public Map<String, Long> sharedKeys;

    private boolean eventlogEnabled = false;
    private String eventlogUrl = "http://localhost:8081/";

    private int eventlogConnections = 50;
    private int eventlogPoolSize = 50;
    private int eventlogSocketTimeout = 500;
    private int eventlogTimeout = 1000;

    private String controllerUrl = "http://localhost:8080/";

    private int controllerConnections = 10;
    private int controllerPoolSize = 10;
    private int controllerSocketTimeout = 500;
    private int controllerTimeout = 1000;

    private String oauth2AccessTokenUrl = null;
    private List<String> oauth2Scopes = Arrays.asList("uid");
    private String oauth2StaticToken = "";

    private boolean dryRun = true;

    public String getGoogleUrl() {
        return googleUrl;
    }

    public void setGoogleUrl(String googleUrl) {
        this.googleUrl = googleUrl;
    }

    public String getGoogleApiKey() {
        return googleApiKey;
    }

    public void setGoogleApiKey(String googleApiKey) {
        this.googleApiKey = googleApiKey;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public boolean isEventlogEnabled() {
        return eventlogEnabled;
    }

    public void setEventlogEnabled(boolean eventlogEnabled) {
        this.eventlogEnabled = eventlogEnabled;
    }

    public String getEventlogUrl() {
        return eventlogUrl;
    }

    public void setEventlogUrl(String eventlogUrl) {
        this.eventlogUrl = eventlogUrl;
    }

    public int getEventlogConnections() {
        return eventlogConnections;
    }

    public void setEventlogConnections(int eventlogConnections) {
        this.eventlogConnections = eventlogConnections;
    }

    public int getEventlogPoolSize() {
        return eventlogPoolSize;
    }

    public void setEventlogPoolSize(int eventlogPoolSize) {
        this.eventlogPoolSize = eventlogPoolSize;
    }

    public int getEventlogSocketTimeout() {
        return eventlogSocketTimeout;
    }

    public void setEventlogSocketTimeout(int eventlogSocketTimeout) {
        this.eventlogSocketTimeout = eventlogSocketTimeout;
    }

    public int getEventlogTimeout() {
        return eventlogTimeout;
    }

    public void setEventlogTimeout(int eventlogTimeout) {
        this.eventlogTimeout = eventlogTimeout;
    }

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

    public String getTwilioApiKey() {
        return twilioApiKey;
    }

    public void setTwilioApiKey(String twilioApiKey) {
        this.twilioApiKey = twilioApiKey;
    }

    public String getTwilioUser() {
        return twilioUser;
    }

    public void setTwilioUser(String twilioUser) {
        this.twilioUser = twilioUser;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTwilioPhoneNumber() {
        return twilioPhoneNumber;
    }

    public void setTwilioPhoneNumber(String twilioPhoneNumber) {
        this.twilioPhoneNumber = twilioPhoneNumber;
    }

    public String getControllerUrl() {
        return controllerUrl;
    }

    public void setControllerUrl(String controllerUrl) {
        this.controllerUrl = controllerUrl;
    }

    public int getControllerConnections() {
        return controllerConnections;
    }

    public void setControllerConnections(int controllerConnections) {
        this.controllerConnections = controllerConnections;
    }

    public int getControllerPoolSize() {
        return controllerPoolSize;
    }

    public void setControllerPoolSize(int controllerPoolSize) {
        this.controllerPoolSize = controllerPoolSize;
    }

    public int getControllerSocketTimeout() {
        return controllerSocketTimeout;
    }

    public void setControllerSocketTimeout(int controllerSocketTimeout) {
        this.controllerSocketTimeout = controllerSocketTimeout;
    }

    public int getControllerTimeout() {
        return controllerTimeout;
    }

    public void setControllerTimeout(int controllerTimeout) {
        this.controllerTimeout = controllerTimeout;
    }

    public String getOauth2AccessTokenUrl() {
        return oauth2AccessTokenUrl;
    }

    public void setOauth2AccessTokenUrl(String oauth2AccessTokenUrl) {
        this.oauth2AccessTokenUrl = oauth2AccessTokenUrl;
    }

    public List<String> getOauth2Scopes() {
        return oauth2Scopes;
    }

    public void setOauth2Scopes(List<String> oauth2Scopes) {
        this.oauth2Scopes = oauth2Scopes;
    }

    public String getOauth2StaticToken() {
        return oauth2StaticToken;
    }

    public void setOauth2StaticToken(String oauth2StaticToken) {
        this.oauth2StaticToken = oauth2StaticToken;
    }
}
