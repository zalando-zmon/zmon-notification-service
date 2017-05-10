package org.zalando.zmon.notifications.pagerduty.webhook.handlers;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.zalando.zmon.notifications.HttpEventLogger;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.pagerduty.client.PagerDutyClient;
import org.zalando.zmon.notifications.pagerduty.webhook.AlertStore;
import org.zalando.zmon.notifications.pagerduty.webhook.PagerDutyWebHookException;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Message;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zalando.zmon.notifications.ZMonEventType.PAGE_ACKNOWLEDGED;
import static org.zalando.zmon.notifications.pagerduty.WebHookTestUtils.*;
import static org.zalando.zmon.notifications.pagerduty.webhook.domain.MessageType.ACKNOWLEDGE;

@RunWith(MockitoJUnitRunner.class)
public class AcknowledgeHandlerTest {
    @Mock
    private PagerDutyClient pagerDutyClient;
    @Mock
    private AlertStore alertStore;
    @Mock
    private HttpEventLogger httpEventLogger;
    @Mock
    private NotificationServiceConfig config;

    @InjectMocks
    private AcknowledgeHandler messageHandler;

    @Test
    public void testHandler() throws Exception {
        when(pagerDutyClient.getAlerts(anyString())).thenReturn(ImmutableList.of(mockAlert("ZMON-12345")));
        final Message message = buildMessage("foo");
        message.getData().getIncident().setAcknowledgers(ImmutableList.of(buildAcknowledger("jd@domain.com")));
        messageHandler.handleMessage(message);
        verify(alertStore).ackAlert(eq(12345));
        verify(httpEventLogger).log(eq(PAGE_ACKNOWLEDGED), eq(12345), eq("foo"), eq("jd@domain.com"));
    }

    @Test(expected = PagerDutyWebHookException.class)
    public void testMissingAcknowledgers() throws Exception {
        when(pagerDutyClient.getAlerts(anyString())).thenReturn(ImmutableList.of(mockAlert("ZMON-12345")));
        final Message message = buildMessage("foo");
        message.getData().getIncident().setAcknowledgers(ImmutableList.of());
        messageHandler.handleMessage(message);
    }

    @Test
    public void testHandlesCorrectMessageType() throws Exception {
        assertThat(messageHandler.getMessageType(), is(ACKNOWLEDGE));
    }
}