package org.zalando.zmon.notifications.pagerduty.webhook;

import com.google.common.base.Strings;
import org.zalando.zmon.notifications.pagerduty.client.Alert;
import org.zalando.zmon.notifications.pagerduty.client.PagerDutyClient;

import java.util.List;
import java.util.Optional;

public final class IntegrationHelper {
    private IntegrationHelper() {}

    public static int alertIdFromAlertKey(final String alertKey) {
        if(Strings.isNullOrEmpty(alertKey)) {
            throw new IllegalArgumentException("alertKey must be a valid, non-empty, string");
        }
        if(!alertKey.startsWith("ZMON-")) {
            throw new IllegalArgumentException("alertKey must have the prefix `ZMON-`");
        }
        return Integer.parseInt(alertKey.replace("ZMON-", ""));
    }

    public static Optional<Integer> getAlertId(final PagerDutyClient client, final String incidentId) {
        final Optional<Alert> firstAlert = getFirstAlertForIncident(client, incidentId);
        return firstAlert.map(alert -> alertIdFromAlertKey(alert.getAlertKey()));
    }

    public static Optional<Alert> getFirstAlertForIncident(final PagerDutyClient client, final String incidentId) {
        final List<Alert> alerts = client.getAlerts(incidentId);
        if (alerts != null && !alerts.isEmpty()) {
            return Optional.of(alerts.get(0));
        }
        return Optional.empty();
    }

}
