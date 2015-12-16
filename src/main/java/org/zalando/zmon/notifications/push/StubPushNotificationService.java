package org.zalando.zmon.notifications.push;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.zalando.zmon.notifications.NotificationServiceApplication;

import java.io.IOException;

public class StubPushNotificationService implements PushNotificationService {

    private final Multimap<String, NotificationServiceApplication.PublishRequestBody> sent = ArrayListMultimap.create();

    @Override
    public void push(NotificationServiceApplication.PublishRequestBody notification, String deviceToken) throws IOException {
        sent.put(deviceToken, notification);
    }

    @Override
    public String toString() {
        return "stub-pushed-notifications: " + sent;
    }
}
