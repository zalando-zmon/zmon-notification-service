package org.zalando.zmon.notifications.push;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.zalando.zmon.notifications.NotificationServiceApplication;

import java.io.IOException;

import static org.zalando.zmon.notifications.json.JsonHelper.jsonEntityFor;

public class GooglePushNotificationService implements PushNotificationService {
    private final String serviceUrl;
    private final Executor executor = Executor.newInstance();

    public GooglePushNotificationService(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    @Override
    public void push(NotificationServiceApplication.PublishRequestBody notification, String deviceToken) throws IOException {
        Response response = executor.execute(
                Request.Post(serviceUrl).body(
                        jsonEntityFor(notification)
                )
        );
    }
}