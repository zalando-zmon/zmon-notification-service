package org.zalando.zmon.notifications;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {NotificationServiceApplication.class})
public class NotificationServiceApplicationTests {

    @Autowired
    NotificationServiceApplication application;

    @Test
    public void contextLoads() {
        assertEquals(
                "GooglePushNotificationService{serviceUrl=https://gcm-http.googleapis.com/gcm/send, googleApiKey=...}",
                application.getPushNotificationService().toString());
    }
}
