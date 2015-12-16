package org.zalando.zmon.notifications.push;

import org.zalando.zmon.notifications.NotificationServiceApplication;

import java.io.IOException;

public interface PushNotificationService {
    void push(NotificationServiceApplication.PublishRequestBody notification, String deviceToken) throws IOException;
}
