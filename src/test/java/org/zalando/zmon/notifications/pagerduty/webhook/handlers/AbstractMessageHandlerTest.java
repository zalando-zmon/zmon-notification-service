package org.zalando.zmon.notifications.pagerduty.webhook.handlers;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.pagerduty.client.PagerDutyClient;
import org.zalando.zmon.notifications.pagerduty.webhook.PagerDutyWebHookException;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Incident;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.zalando.zmon.notifications.pagerduty.WebHookTestUtils.*;

@RunWith(MockitoJUnitRunner.class)
public class AbstractMessageHandlerTest {
    @Mock
    private PagerDutyClient client;
    @Mock
    private NotificationServiceConfig config;

    private AbstractMessageHandler messageHandler = mock(AbstractMessageHandler.class, CALLS_REAL_METHODS);

    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(messageHandler, "client", client);
        ReflectionTestUtils.setField(messageHandler, "config", config);
        ReflectionTestUtils.setField(messageHandler, "log", mock(Logger.class));
        when(messageHandler.getUsername(any(Incident.class))).thenReturn("johndoe");
    }

    @Test
    public void testDelegate() throws Exception {
        when(client.getAlerts(anyString())).thenReturn(ImmutableList.of(mockAlert("ZMON-12345")));
        messageHandler.handleMessage(buildMessage("foo"));
        verify(messageHandler).doHandleMessage(eq(12345), eq("foo"), eq("johndoe"));
    }

    @Test(expected = PagerDutyWebHookException.class)
    public void testNoAlertId() throws Exception {
        when(client.getAlerts(anyString())).thenReturn(ImmutableList.of());
        messageHandler.handleMessage(buildMessage("foo"));
    }

    @Test
    public void testDryRun() throws Exception {
        when(config.isDryRun()).thenReturn(Boolean.TRUE);
        when(client.getAlerts(anyString())).thenReturn(ImmutableList.of(mockAlert("ZMON-12345")));
        messageHandler.handleMessage(buildMessage("foo"));
        verify(messageHandler, never()).doHandleMessage(anyInt(), anyString(), anyString());
    }


}