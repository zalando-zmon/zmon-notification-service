package org.zalando.zmon.notifications.store;

import java.util.Collection;

public interface NotificationStore {
    void addDeviceForUid(String deviceId, String uid);

    void removeDeviceForUid(String deviceId, String uid);

    Collection<String> devicesForUid(String uid);

    Collection<Integer> alertsForUid(String uid);

    void addAlertForUid(int alertId, String uid);

    void removeAlertForUid(int alertId, String uid);

    void addTeamToUid(String team, String uid);

    void removeTeamFromUid(String team, String uid);

    Collection<String> teamsForUid(String uid);

    Collection<String> devicesForAlerts(int alertId, String team, int priority);

    Collection<String> getAllDeviceIds();

    void setPriority(int priority, String uid);

    int getPriority(String uid);
}
