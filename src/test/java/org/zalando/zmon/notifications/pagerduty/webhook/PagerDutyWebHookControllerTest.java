package org.zalando.zmon.notifications.pagerduty.webhook;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zalando.zmon.notifications.HttpEventLogger;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.pagerduty.client.PagerDutyClient;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.zalando.zmon.notifications.ZMonEventType.PAGE_ACKNOWLEDGED;
import static org.zalando.zmon.notifications.pagerduty.client.ClientTestUtils.mockAlert;

@RunWith(MockitoJUnitRunner.class)
public class PagerDutyWebHookControllerTest {
    @Mock
    private TokenInfoService tokenInfoService;
    @Mock
    private PagerDutyClient pagerDutyClient;
    @Mock
    private AlertStore alertStore;
    @Mock
    private NotificationServiceConfig config;
    @Mock
    private HttpEventLogger httpEventLogger;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        final Object webHookController = new PagerDutyWebHookController(pagerDutyClient, tokenInfoService,
                config, httpEventLogger, alertStore);
        mockMvc = MockMvcBuilders.standaloneSetup(webHookController).alwaysDo(print()).build();
    }

    @Test
    public void testAcknowledge() throws Exception {
        when(tokenInfoService.lookupUid(anyString())).thenReturn(Optional.of("test"));
        when(pagerDutyClient.getAlerts(anyString())).thenReturn(ImmutableList.of(mockAlert("ZMON-12345")));
        mockMvc.perform(post("/api/v1/pagerduty/webhook")
                .param("psk", "test-psk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testPayload("/pagerduty_webhook.json")))
                .andExpect(status().isOk());
        verify(alertStore).ackAlert(eq(12345), anyString());
        verify(httpEventLogger).log(eq(PAGE_ACKNOWLEDGED), eq(12345), anyString());
    }

    @Test
    public void testMissingAuth() throws Exception {
        when(tokenInfoService.lookupUid(anyString())).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/v1/pagerduty/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testPayload("/pagerduty_webhook.json")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testNoAlertsFromPagerDuty() throws Exception {
        when(tokenInfoService.lookupUid(anyString())).thenReturn(Optional.of("test"));
        when(pagerDutyClient.getAlerts(anyString())).thenReturn(ImmutableList.of());
        mockMvc.perform(post("/api/v1/pagerduty/webhook")
                .param("psk", "test-psk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testPayload("/pagerduty_webhook.json")))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testMissingAcknowledgers() throws Exception {
        when(tokenInfoService.lookupUid(anyString())).thenReturn(Optional.of("test"));
        mockMvc.perform(post("/api/v1/pagerduty/webhook")
                .param("psk", "test-psk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testPayload("/pagerduty_webhook_no_acknowledgers.json")))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testDryRun() throws Exception {
        when(config.isDryRun()).thenReturn(Boolean.TRUE);
        when(tokenInfoService.lookupUid(anyString())).thenReturn(Optional.of("test"));
        when(pagerDutyClient.getAlerts(anyString())).thenReturn(ImmutableList.of(mockAlert("ZMON-12345")));
        mockMvc.perform(post("/api/v1/pagerduty/webhook")
                .param("psk", "test-psk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testPayload("/pagerduty_webhook.json")))
                .andExpect(status().isOk());
        verify(alertStore, never()).ackAlert(anyInt(), anyString());
        verify(httpEventLogger, never()).log(any(), anyInt(), anyString());
    }

    @Test
    public void testUnsupportedMessageTypes() throws Exception {
        when(tokenInfoService.lookupUid(anyString())).thenReturn(Optional.of("test"));
        mockMvc.perform(post("/api/v1/pagerduty/webhook")
                .param("psk", "test-psk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testPayload("/pagerduty_webhook_unsupported_msg_type.json")))
                .andExpect(status().isOk());
        verify(pagerDutyClient, never()).getAlerts(anyString());
        verify(alertStore, never()).ackAlert(anyInt(), anyString());
        verify(httpEventLogger, never()).log(any(), anyInt(), anyString());

    }

    private String testPayload(final String name) throws IOException {
        final URL url = this.getClass().getResource(name);
        return Resources.toString(url, Charsets.UTF_8);
    }
}