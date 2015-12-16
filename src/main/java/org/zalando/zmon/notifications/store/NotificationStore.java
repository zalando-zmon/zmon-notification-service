package org.zalando.zmon.notifications.store;

import java.util.Collection;

public interface NotificationStore {
    void addDeviceForUid(String deviceId, String uid);
    void addAlertForUid(int alertId, String uid);

    void removeDeviceForUid(String deviceId, String uid);
    void removeAlertForUid(int alertId, String uid);

    Collection<String> devicesForAlerts(int alertId);
}
