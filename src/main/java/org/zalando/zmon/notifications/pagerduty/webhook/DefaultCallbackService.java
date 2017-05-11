package org.zalando.zmon.notifications.pagerduty.webhook;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Message;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.MessageType;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultCallbackService implements CallbackService {
    private final Logger log = LoggerFactory.getLogger(DefaultCallbackService.class);

    private final Map<MessageType, List<MessageHandler>> messageHandlers;

    public DefaultCallbackService(@NotNull final List<MessageHandler> messageHandlers) {
        Preconditions.checkNotNull(messageHandlers, "DefaultCallbackService(): messageHandlers can't be null");
        this.messageHandlers = messageHandlers.stream()
                .collect(Collectors.groupingBy(
                        MessageHandler::getMessageType,
                        Collectors.mapping(Function.identity(), Collectors.toList())
                ));
    }

    @Override
    public void handledMessages(@NotNull List<Message> messages) {
        Preconditions.checkNotNull(messages, "handleMessages(): messages can't be null");
        for (final Message message: messages) {
            final MessageType messageType = message.getType();
            final List<MessageHandler> messageHandlersForThisType = messageHandlers.get(messageType);
            if(messageHandlersForThisType != null) {
                for (final MessageHandler messageHandler : messageHandlersForThisType) {
                    log.debug("{} handling PagerDuty {}, id={}", messageHandler.getClass().getSimpleName(), messageType, message.getId());
                    messageHandler.handleMessage(message);
                }
            } else {
                throw new PagerDutyWebHookException("No handler for message type %s", messageType);
            }
        }
    }
}
