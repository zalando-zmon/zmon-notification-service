package org.zalando.zmon.notifications;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by jmussler on 12/16/15.
 */
@Configuration
@ConfigurationProperties(prefix = "mobile")
public class MobileAPIConfig {
    public String dataServiceURL;
    public String readScope;

    public String getDataServiceURL() {
        return dataServiceURL;
    }

    public void setDataServiceURL(String dataServiceURL) {
        this.dataServiceURL = dataServiceURL;
    }
}
