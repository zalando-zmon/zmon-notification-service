package org.zalando.zmon.notifications.pagerduty;

import org.zalando.zmon.notifications.pagerduty.client.Alert;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Acknowledger;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Data;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Incident;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Message;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.MessageType;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.User;

import java.util.Date;

import static org.zalando.zmon.notifications.pagerduty.webhook.domain.MessageType.ACKNOWLEDGE;

public final class WebHookTestUtils {
    private WebHookTestUtils() {}

    public static Alert mockAlert(final String key) {
        final Alert alert = new Alert();
        alert.setAlertKey(key);
        return alert;
    }

    public static Message buildMessage(final String incidentId) {
        return buildMessage(incidentId, ACKNOWLEDGE);
    }

    public static Message buildMessage(final String incidentId, final MessageType type) {
        final Message message = new Message();
        message.setId(incidentId);
        message.setCreatedOn(new Date());
        message.setType(type);
        final Incident incident = new Incident();
        incident.setId(incidentId);
        final Data data = new Data();
        data.setIncident(incident);
        message.setData(data);
        return message;
    }

    public static Acknowledger buildAcknowledger(final String email) {
        final Acknowledger acknowledger = new Acknowledger();
        acknowledger.setAt(new Date());
        acknowledger.setObject(buildUser(email));
        return acknowledger;
    }

    public static User buildUser(final String email) {
        final User user = new User();
        user.setName("John Doe");
        user.setEmail(email);
        return user;
    }
}
