package org.zalando.zmon.notifications.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.zmon.notifications.TwilioAlert;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Set;
import java.util.UUID;

/**
 * Created by jmussler on 11.10.16.
 */
public class TwilioNotificationStore {

    private final ObjectMapper mapper;

    private final JedisPool pool;

    public TwilioNotificationStore(JedisPool pool, ObjectMapper mapper) {
        this.pool = pool;
        this.mapper = mapper;
    }

    public TwilioAlert getAlert(String uuid) {
        if (null == uuid || "".equals(uuid)) {
            return null;
        }

        try(Jedis jedis = pool.getResource()) {
            String data = jedis.get(uuid);
            if (null == data) {
                return null;
            }
            return mapper.readValue(data, TwilioAlert.class);
        }
        catch(Exception ex) {
            return null;
        }
    }

    public boolean lockAlert(int alertId, String entityId) {
        try(Jedis jedis = pool.getResource()) {
            final String lockKey = "zmon:notify:lock:" + alertId;
            long l = jedis.setnx(lockKey, entityId);
            if(l > 0) {
                jedis.expire(lockKey, 120);
                return true;
            }
        }
        catch(Exception ex) {

        }
        return false;
    }

    public String storeAlert(TwilioAlert data) {

        try(Jedis jedis = pool.getResource()) {
            String uuid = UUID.randomUUID().toString();
            jedis.set(uuid, mapper.writeValueAsString(data));
            jedis.expire(uuid, 60*60);
            return uuid;
        }
        catch(Exception ex) {

        }
        return null;
    }

    public boolean ackAlert(int alertId, String user) {
        try(Jedis jedis = pool.getResource()) {
            final String key ="zmon:notify:ack:" + alertId;
            jedis.set(key, user);
            jedis.expire(key, 60 * 60);
        }
        catch(Exception ex) {
            return false;
        }
        return true;
    }

    public boolean resolveAlert(int alertId) {
        try(Jedis jedis = pool.getResource()) {
            final String key = "zmon:notify:ack:" + alertId;
            jedis.del(key);

            Set<String> keys = jedis.keys("zmon:notifiy:ack:" + alertId + ":*");
            for(String k : keys) {
                jedis.del(k);
            }

        }
        catch(Exception ex) {
            return false;
        }
        return true;
    }

    public boolean ackAlertEntity(int alertId, String entityId, String user) {
        try(Jedis jedis = pool.getResource()) {
            final String key ="zmon:notify:ack:" + alertId + ":" + entityId;
            jedis.set(key, user);
            jedis.expire(key, 60 * 60);
        }
        catch(Exception ex) {
            return false;
        }
        return true;
    }

    public boolean isAck(int alertId, String entityId) {
        try(Jedis jedis = pool.getResource()) {
            String key ="zmon:notify:ack:" + alertId;
            String value = jedis.get(key);
            if (null != value) {
                return true;
            }

            key ="zmon:notify:ack:" + alertId + ":" + entityId;
            value = jedis.get(key);
            if (null != value) {
                return true;
            }
        }
        catch(Exception ex) {
            return false;
        }
        return false;
    }
}
