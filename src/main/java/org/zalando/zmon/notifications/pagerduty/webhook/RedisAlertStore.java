package org.zalando.zmon.notifications.pagerduty.webhook;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisAlertStore implements AlertStore {
    @VisibleForTesting
    static final String ZMON_ALERT_ACKS = "zmon:alert-acks";

    private final Logger log = LoggerFactory.getLogger(RedisAlertStore.class);

    private final JedisPool pool;

    public RedisAlertStore(final JedisPool pool) {
        this.pool = pool;
    }

    @Override
    public void ackAlert(int alertId) {
        try(final Jedis jedis = pool.getResource()) {
            jedis.sadd(ZMON_ALERT_ACKS, Integer.toString(alertId));
        } catch(Exception ex) {
            throw new AlertStoreException(String.format("Failed to add alert #%d to %s", alertId, ZMON_ALERT_ACKS), ex);
        }
    }

    @Override
    public void unackAlert(final int alertId) {
        try(final Jedis jedis = pool.getResource()) {
            jedis.srem(ZMON_ALERT_ACKS, Integer.toString(alertId));
        } catch(Exception ex) {
            throw new AlertStoreException(String.format("Failed to remove alert #%d to %s", alertId, ZMON_ALERT_ACKS), ex);
        }
    }
}
