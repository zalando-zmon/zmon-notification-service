package org.zalando.zmon.notifications.api;

/**
 * Created by jmussler on 13.11.16.
 */ // request payloads
public class DeviceRequestBody {
    public String registrationToken;

    public DeviceRequestBody() {};

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    @Override
    public String toString() {
        return "DeviceRequestBody{" +
                "registrationToken='" + registrationToken + '\'' +
                '}';
    }
}
