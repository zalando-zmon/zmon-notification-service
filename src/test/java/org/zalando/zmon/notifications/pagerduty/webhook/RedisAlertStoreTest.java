package org.zalando.zmon.notifications.pagerduty.webhook;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zalando.zmon.notifications.pagerduty.webhook.RedisAlertStore.ZMON_ALERT_ACKS;

@RunWith(MockitoJUnitRunner.class)
public class RedisAlertStoreTest {
    @Mock
    private Jedis jedis;

    @Mock
    private JedisPool jedisPool;

    @InjectMocks
    private RedisAlertStore redisAlertStore;

    @Before
    public void setUp() throws Exception {
        when(jedisPool.getResource()).thenReturn(jedis);
    }

    @Test
    public void testAck() throws Exception {

        when(jedis.sadd(anyString(), anyString())).thenReturn(Long.MAX_VALUE);
        redisAlertStore.ackAlert(12345);
        verify(jedis).sadd(ZMON_ALERT_ACKS, "12345");
    }

    @Test(expected = AlertStoreException.class)
    public void testAckRedisFailure() throws Exception {
        when(jedis.sadd(anyString(), anyString())).thenThrow(new RuntimeException("shit has hit the fan"));
        redisAlertStore.ackAlert(12345);
        verify(jedis).sadd(ZMON_ALERT_ACKS, "12345");
    }

    @Test
    public void testUnack() throws Exception {
        when(jedis.srem(anyString(), anyString())).thenReturn(Long.MAX_VALUE);
        redisAlertStore.unackAlert(12345);
        verify(jedis).srem(ZMON_ALERT_ACKS, "12345");
    }

    @Test(expected = AlertStoreException.class)
    public void testUnackRedisFailure() throws Exception {
        when(jedis.srem(anyString(), anyString())).thenThrow(new RuntimeException("shit has hit the fan"));
        redisAlertStore.unackAlert(12345);
        verify(jedis).srem(ZMON_ALERT_ACKS, "12345");
    }

}