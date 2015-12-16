package org.zalando.zmon.notifications;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.zmon.notifications.push.GooglePushNotificationService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = NotificationServiceApplication.class)
public class NotificationServiceApplicationTests {

	@Autowired
	NotificationServiceApplication application;

	@Test
	public void contextLoads() {
		assertEquals(
				"GooglePushNotificationService{serviceUrl=someUrl, googleApiKey=someKey}",
				application.getPushNotificationService().toString());
		assertEquals(
				"OAuthTokenInfoService{serviceUrl=someInfoServiceUrl}",
				application.getTokenInfoService().toString());
	}

}
