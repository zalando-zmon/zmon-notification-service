package org.zalando.zmon.notifications.api;

import com.google.common.base.MoreObjects;

/**
 * Created by jmussler on 13.11.16.
 */ // defined by google cloud messaging API
public class PublishNotificationPart {
    public String title = "";
    public String body = "";
    public String icon = "";
    public String click_action = "";

    public PublishNotificationPart() {

    }

    public PublishNotificationPart(String t, String b, String i, String c) {
        icon = i;
        body = b;
        title = t;
        click_action = c;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("title", title)
                .add("body", body)
                .add("icon", icon).toString();
    }
}
