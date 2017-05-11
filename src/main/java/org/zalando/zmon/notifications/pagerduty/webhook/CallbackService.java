package org.zalando.zmon.notifications.pagerduty.webhook;

import org.zalando.zmon.notifications.pagerduty.webhook.domain.Message;

import java.util.List;

public interface CallbackService {
    void handledMessages(List<Message> messages);
}
