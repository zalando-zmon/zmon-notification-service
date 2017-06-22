package org.zalando.zmon.notifications.opsgenie;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.zmon.notifications.HttpEventLogger;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.pagerduty.webhook.AlertStore;

/**
 * Created by mabdelhameed on 21/06/2017.
 */
@Configuration
public class OpsgenieConfig {

    @Bean
    public ActionHandler opsgenieActionHandler(NotificationServiceConfig config, AlertStore alertStore, HttpEventLogger eventLog) {
        return new ActionHandler(config, alertStore, eventLog);
    }
}
