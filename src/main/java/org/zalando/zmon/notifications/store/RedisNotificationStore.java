package org.zalando.zmon.notifications.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RedisNotificationStore implements NotificationStore {

    private final JedisPool jedisPool;
    private final Logger log = LoggerFactory.getLogger(RedisNotificationStore.class);
    private final ObjectMapper mapper;

    public RedisNotificationStore(JedisPool jedisPool, ObjectMapper mapper) {
        this.jedisPool = jedisPool;
        this.mapper = mapper;
    }

    public static class DeviceData {
        public long lastRegistered;

        public DeviceData() {}

        public DeviceData(long lastRegistered)  {
            this.lastRegistered = lastRegistered;
        }
    }

    // store map of deviceids to user with last seen, we can filter and cleanup later
    @Override
    public void addDeviceForUid(String deviceId, String uid) {
        try (Jedis jedis = jedisPool.getResource()) {
            String deviceData = mapper.writeValueAsString(new DeviceData(System.currentTimeMillis()));
            jedis.hset(devicesForUidKey(uid), deviceId, deviceData);
            jedis.hset(globalDeviceIdsKey(), deviceId, deviceData);
        }
        catch(IOException ex) {
            log.error("Registration failed", ex);
        }
    }

    @Override
    public void removeDeviceForUid(String deviceId, String uid) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hdel(globalDeviceIdsKey(), deviceId);
            jedis.hdel(devicesForUidKey(uid), deviceId); // remove device from user
        }
    }

    @Override
    public Collection<String> devicesForUid(String uid) {
        try(Jedis jedis = jedisPool.getResource()) {
            return jedis.hkeys(devicesForUidKey(uid));
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
    public Collection<String> devicesForAlerts(int alertId, String team) {
        HashSet<String> deviceIds = new HashSet<>();
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> teamMembers = jedis.smembers(uidsForTeamKey(team));
            Set<String> subscribers = jedis.smembers(notificationsForAlertKey(alertId));

            Set<String> recipients = new HashSet<>();
            if (teamMembers != null && teamMembers.size() > 0) {
                recipients.addAll(teamMembers);
            }

            if (subscribers != null && subscribers.size() > 0) {
                recipients.addAll(subscribers);
            }

            for (String uid : teamMembers) {
                deviceIds.addAll(jedis.smembers(devicesForUidKey(uid)));
            }
        }
        return deviceIds;
    }

    @Override
    public void addTeamToUid(String team, String uid) {
        try(Jedis jedis = jedisPool.getResource()) {
            jedis.sadd(teamsForUidKey(uid), team);
            jedis.sadd(uidsForTeamKey(team), uid);
        }
    }

    @Override
    public void removeTeamFromUid(String team, String uid) {
        try(Jedis jedis = jedisPool.getResource()) {
            jedis.srem(teamsForUidKey(uid), team);
            jedis.srem(uidsForTeamKey(team), uid);
        }
    }

    @Override
    public Collection<String> teamsForUid(String uid) {
        try(Jedis jedis = jedisPool.getResource()) {
            Set<String> set =jedis.smembers(teamsForUidKey(uid));
            return set;
        }
    }

    @Override
    public Collection<String> getAllDeviceIds() {
        try(Jedis jedis = jedisPool.getResource()) {
            return jedis.hkeys(globalDeviceIdsKey());
        }
    }

    // helpers

    // build redis key for sets containing all devices for a given uid
    private String devicesForUidKey(String uid) {
        return String.format("zmon:push:user-devices:%s", uid);
    }

    private String alertsForUidKey(String uid) {
        return "zmon:push:alerts-for-uid:" + uid;
    }

    private String teamsForUidKey(String uid) {
        return "zmon:push:teams-for-uid:" + uid;
    }

    private String uidsForTeamKey(String team) {
        return "zmon:push:uids-for-team:" + team;
    }

    private String globalDeviceIdsKey() { return "zmon:push:global-devices"; }

    // build redis key for sets containing all devices subscribed to given alertId
    private String notificationsForAlertKey(int alertId) {
        return String.format("zmon:push:uids-for-alert:%d", alertId);
    }
}
