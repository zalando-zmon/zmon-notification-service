package org.zalando.zmon.notifications.pagerduty.client;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestOperations;

import java.util.List;

import static org.zalando.zmon.notifications.config.NotificationServiceConfig.PAGERDUTY_API_DEFAULT_URL;

public class DefaultClient extends AbstractResilientClient implements PagerDutyClient {
    private final Logger log = LoggerFactory.getLogger(DefaultClient.class);

    private final String baseUrl;
    private final RestOperations restOperations;

    public DefaultClient(final RestOperations restOperations) {
        this(restOperations, PAGERDUTY_API_DEFAULT_URL);
    }

    public DefaultClient(final RestOperations restOperations, String baseUrl) {
        this.baseUrl = Strings.isNullOrEmpty(baseUrl) ? PAGERDUTY_API_DEFAULT_URL : baseUrl;
        this.restOperations = restOperations;
    }

    @Override
    public List<Alert> getAlerts(final String incidentId) {
        final AlertsResponse response = doResilientCall(() -> doGetAlerts(incidentId));
        return response.getAlerts();
    }

    private AlertsResponse doGetAlerts(final String incidentId) {
        final String url = String.format("%s/incidents/%s/alerts", baseUrl, incidentId);
        log.info("Loading PagerDuty alerts {}", url);
        return restOperations.getForObject(url, AlertsResponse.class);
    }
}
