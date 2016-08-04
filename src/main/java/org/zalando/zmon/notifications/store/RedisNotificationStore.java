package org.zalando.zmon.notifications.store;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class RedisNotificationStore implements NotificationStore {

    private final JedisPool jedisPool;

    public RedisNotificationStore(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public void addDeviceForUid(String deviceId, String uid) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.sadd(devicesForUidKey(uid), deviceId);     // this redis set contains all the devices registered for a specific oauth uid
        }
    }

    @Override
    public void addAlertForUid(int alertId, String uid) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.sadd(alertsForUidKey(uid), "" + alertId);
            jedis.sadd(notificationsForAlertKey(alertId), uid);     // this redis set contains all the users registered for a specific alert id
        }
    }

    @Override
    public void removeDeviceForUid(String deviceId, String uid) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.srem(devicesForUidKey(uid), deviceId); // remove device from user
        }
    }

    @Override
    public void removeAlertForUid(int alertId, String uid) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.srem(alertsForUidKey(uid), "" + alertId);
            jedis.srem(notificationsForAlertKey(alertId), uid);
        }
    }

    @Override
    public Collection<Integer> alertsForUid(String uid) {
        try (Jedis jedis = jedisPool.getResource()) {
            List<Integer> alertIds = jedis.smembers(alertsForUidKey(uid)).stream().map(Integer::parseInt).collect(Collectors.toList());
            return alertIds;
        }
    }

    @Override
    public Collection<String> devicesForAlerts(int alertId) {
        HashSet<String> deviceIds = new HashSet<>();
        try (Jedis jedis = jedisPool.getResource()) {
            for (String uid : jedis.smembers(notificationsForAlertKey(alertId))) {
                deviceIds.addAll(jedis.smembers(devicesForUidKey(uid)));
            }
        }
        return deviceIds;
    }

    // helpers


    // build redis key for sets containing all devices for a given uid
    private String devicesForUidKey(String uid) {
        return String.format("zmon:push:user-devices:%s", uid);
    }

    private String alertsForUidKey(String uid) {
        return "zmon:push:alerts-for-uid:" + uid;
    }

    // build redis key for sets containing all devices subscribed to given alertId
    private String notificationsForAlertKey(int alertId) {
        return String.format("zmon:push:uids-for-alert:%d", alertId);
    }
}
