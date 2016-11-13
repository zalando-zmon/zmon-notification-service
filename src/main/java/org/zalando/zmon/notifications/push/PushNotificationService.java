package org.zalando.zmon.notifications.push;

import org.zalando.zmon.notifications.api.PublishRequestBody;

import java.io.IOException;

public interface PushNotificationService {
    void push(PublishRequestBody notification, String deviceToken) throws IOException;
}
