package org.zalando.zmon.notifications;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.zmon.notifications.oauth.DummyTokenInfoService;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.push.PushNotificationService;
import org.zalando.zmon.notifications.push.StubPushNotificationService;
import org.zalando.zmon.notifications.store.InMemoryNotificationStore;
import org.zalando.zmon.notifications.store.NotificationStore;

import java.net.URISyntaxException;
import java.util.UUID;

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
    public void add() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        String deviceId = UUID.randomUUID().toString();

        mvc.perform(
                post("/api/v1/device").
                        content("{\"registration_token\" : \""+deviceId+"\" }")
//                        accept("application/json")
        ).
                andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));


        assertNotNull(wac);
//        RestTemplate template = new TestRestTemplate();
//
//        template.postForEntity("")
//        when().
//                post("/api/v1/device", "").
//                then().
//                statusCode(HttpStatus.SC_OK).
//                body("name", Matchers.is("Mickey Mouse")).
//                body("id", Matchers.is(mickeyId));
    }
}
