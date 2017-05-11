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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zalando.zmon.notifications.ZMonEventType.PAGE_UNACKNOWLEDGED;
import static org.zalando.zmon.notifications.pagerduty.WebHookTestUtils.buildMessage;
import static org.zalando.zmon.notifications.pagerduty.WebHookTestUtils.mockAlert;
import static org.zalando.zmon.notifications.pagerduty.webhook.domain.MessageType.UNACKNOWLEDGE;
import static org.zalando.zmon.notifications.pagerduty.webhook.handlers.UnacknowledgeHandler.UNACK_USERNAME;

@RunWith(MockitoJUnitRunner.class)
public class UnacknowledgeHandlerTest {
    @Mock
    private PagerDutyClient pagerDutyClient;
    @Mock
    private AlertStore alertStore;
    @Mock
    private HttpEventLogger httpEventLogger;
    @Mock
    private NotificationServiceConfig config;

    @InjectMocks
    private UnacknowledgeHandler messageHandler;

    @Test
    public void testHandler() throws Exception {
        when(pagerDutyClient.getAlerts(anyString())).thenReturn(ImmutableList.of(mockAlert("ZMON-12345")));
        messageHandler.handleMessage(buildMessage("foo", UNACKNOWLEDGE));
        verify(alertStore).unackAlert(eq(12345));
        verify(httpEventLogger).log(eq(PAGE_UNACKNOWLEDGED), eq(12345), eq("foo"), eq(UNACK_USERNAME));
    }

    @Test
    public void testHandlesCorrectMessageType() throws Exception {
        assertThat(messageHandler.getMessageType(), is(UNACKNOWLEDGE));
    }
}