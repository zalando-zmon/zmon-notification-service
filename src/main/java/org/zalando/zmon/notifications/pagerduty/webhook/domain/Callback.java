package org.zalando.zmon.notifications.pagerduty.webhook.domain;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class Callback {
    private List<Message> messages = ImmutableList.of();

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
