package org.zalando.zmon.notifications.pagerduty.webhook;

import org.zalando.zmon.notifications.pagerduty.webhook.domain.Message;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.MessageType;

public interface MessageHandler {
    MessageType getMessageType();
    void handleMessage(final Message message);
}
