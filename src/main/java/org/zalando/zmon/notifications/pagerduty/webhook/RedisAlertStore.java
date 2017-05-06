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
    public void ackAlert(int alertId, String userName) {
        try(final Jedis jedis = pool.getResource()) {
            jedis.sadd(ZMON_ALERT_ACKS, Integer.toString(alertId));
        } catch(Exception ex) {
            log.error("failed to ACK alert {} from user {}", alertId, userName, ex);
        }
    }
}
