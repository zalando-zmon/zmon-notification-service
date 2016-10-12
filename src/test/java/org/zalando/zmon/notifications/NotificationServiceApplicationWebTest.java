package org.zalando.zmon.notifications;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.zmon.notifications.oauth.DummyTokenInfoService;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.push.PushNotificationService;
import org.zalando.zmon.notifications.push.StubPushNotificationService;
import org.zalando.zmon.notifications.store.InMemoryNotificationStore;
import org.zalando.zmon.notifications.store.NotificationStore;

import java.net.URISyntaxException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {NotificationServiceApplication.class, NotificationServiceApplicationWebTest.class})
@WebIntegrationTest({"server.port=0", "management.port=0"})
public class NotificationServiceApplicationWebTest {

    @Autowired
    NotificationServiceApplication application;

    @Autowired
    private WebApplicationContext wac;


    private final String DEVICE = UUID.fromString("6f18fb92-dbe3-41ac-ab8e-82be7f30e246").toString();
    private final int ALERT = 142;


    @Bean
    TokenInfoService getTokenInfoService() {
        return new DummyTokenInfoService();
    }

    @Bean
    PushNotificationService getPushNotificationService() {
        return new StubPushNotificationService();
    }

    @Bean
    NotificationStore getNotificationStore() throws URISyntaxException {
        return new InMemoryNotificationStore();
    }

    @Test
    public void unauthorized() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        mvc.perform(
                post("/api/v1/device").
                        contentType(MediaType.APPLICATION_JSON_UTF8).
                        content("{\"registration_token\" : \"" + DEVICE + "\" }").
                        header("Authorization", "Bearer 1334ff68-ba2e-4b07-8e67-9304c55f8308")  // wrong token; see: DummyTokenInfoService
        ).andExpect(
                status().isUnauthorized()
        );
    }

    @Test
    public void happyPath() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        // insert device
        mvc.perform(
                post("/api/v1/device").
                        contentType(MediaType.APPLICATION_JSON_UTF8).
                        content("{\"registration_token\" : \""+ DEVICE +"\" }").
                        header("Authorization", "Bearer 6334ff68-ba2e-4b07-8e67-9304c55f8308")
        ).andExpect(
                status().isOk()
        );

        assertEquals(
                "in-mem-store: devices={a-uid=[6f18fb92-dbe3-41ac-ab8e-82be7f30e246]} alerts={}",
                application.notificationStore.toString()
        );

        // insert alert
        mvc.perform(
                post("/api/v1/subscription").
                        contentType(MediaType.APPLICATION_JSON_UTF8).
                        content("{\"alert_id\" : "+ ALERT +" }").
                        header("Authorization", "Bearer 6334ff68-ba2e-4b07-8e67-9304c55f8308")
        ).andExpect(
                status().isOk()
        );

        assertEquals(
                "in-mem-store: devices={a-uid=[6f18fb92-dbe3-41ac-ab8e-82be7f30e246]} alerts={142=[a-uid]}",
                application.notificationStore.toString()
        );

        // publish
        mvc.perform(
                post("/api/v1/publish").
                        header("Authorization", "Bearer 6334ff68-ba2e-4b07-8e67-9304c55f8308").
                        contentType(MediaType.APPLICATION_JSON_UTF8).
                        content("{" +
                                    "\"alert_id\" : "+ ALERT + "," +
                                    "\"entity_id\":\"customer5.db.zalando\"," +
                                    "\"notification\": {\"title\":\"No database connection to master\",\"body\":\"\",\"icon\":\"\"}" +
                                "}"
                        )
        ).andExpect(
                status().isOk()
        );

        assertEquals(
                "stub-pushed-notifications: {6f18fb92-dbe3-41ac-ab8e-82be7f30e246=[PublishRequestBody{alertId="+ALERT+", notification=PublishNotificationPart{title=No database connection to master, body=, icon=}, entityId=customer5.db.zalando}]}",
                application.pushNotificationService.toString()
        );
    }
}
