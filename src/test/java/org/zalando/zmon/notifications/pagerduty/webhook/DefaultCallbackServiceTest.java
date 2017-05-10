package org.zalando.zmon.notifications.pagerduty.webhook;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Message;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.MessageType;

import java.util.*;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.zalando.zmon.notifications.pagerduty.WebHookTestUtils.buildMessage;
import static org.zalando.zmon.notifications.pagerduty.webhook.domain.MessageType.TRIGGER;

@RunWith(MockitoJUnitRunner.class)
public class DefaultCallbackServiceTest {

    @Test
    public void testMessageHandling() throws Exception {
        final EnumSet<MessageType> testTypes = EnumSet.allOf(MessageType.class);
        final Map<MessageType, MessageHandler> handlers = new HashMap<>(testTypes.size());
        final Map<MessageType, Message> messages = new HashMap<>(testTypes.size());

        for (final MessageType testType : testTypes) {
            final MessageHandler handler = mock(MessageHandler.class);
            when(handler.getMessageType()).thenReturn(testType);
            handlers.put(testType, handler);
            final Message message = buildMessage("msg-" + testType, testType);
            messages.put(testType, message);
        }

        final CallbackService callbackService = new DefaultCallbackService(ImmutableList.copyOf(handlers.values()));
        callbackService.handledMessages(ImmutableList.copyOf(messages.values()));

        for (final MessageType testType : testTypes) {
            final MessageHandler handler = handlers.get(testType);
            final Message message = messages.get(testType);
            verify(handler).handleMessage(eq(message));
        }
    }

    @Test(expected = PagerDutyWebHookException.class)
    public void testMissingHandler() throws Exception {
        final CallbackService callbackService = new DefaultCallbackService(ImmutableList.of());
        callbackService.handledMessages(ImmutableList.of(buildMessage("fail")));
    }

    @Test
    public void testMultipleHandlersForSameMessageType() throws Exception {
        final List<MessageHandler> handlers = ImmutableList.of(mock(MessageHandler.class), mock(MessageHandler.class));
        for (final MessageHandler handler : handlers) {
            when(handler.getMessageType()).thenReturn(TRIGGER);
        }
        final CallbackService callbackService = new DefaultCallbackService(handlers);
        callbackService.handledMessages(ImmutableList.of(buildMessage("multiple-handlers", TRIGGER)));
        for (final MessageHandler handler : handlers) {
            verify(handler).handleMessage(any(Message.class));
        }
    }
}