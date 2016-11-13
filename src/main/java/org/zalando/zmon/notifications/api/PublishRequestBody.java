package org.zalando.zmon.notifications.api;

import com.google.common.base.MoreObjects;

/**
 * Created by jmussler on 13.11.16.
 */
public class PublishRequestBody {
    public int alertId;
    public String entityId;
    public String team;
    public String responsibleTeam;
    public PublishNotificationPart notification;

    public PublishNotificationPart getNotification() {
        return notification;
    }

    public void setNotification(PublishNotificationPart notification) {
        this.notification = notification;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("alertId", alertId)
                .add("notification", notification)
                .add("entityId", entityId)
                .toString();
    }
}
