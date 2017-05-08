package org.zalando.zmon.notifications.pagerduty;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.pagerduty.client.DefaultClient;
import org.zalando.zmon.notifications.pagerduty.client.PagerDutyClient;
import org.zalando.zmon.notifications.pagerduty.client.PagerDutyClientInterceptor;
import org.zalando.zmon.notifications.pagerduty.webhook.AlertStore;
import org.zalando.zmon.notifications.pagerduty.webhook.RedisAlertStore;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class Config {
    @Bean()
    public RestOperations pagerDutyRestOperations(final NotificationServiceConfig config) {
        final RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory(config));
        restTemplate.getInterceptors().add(new PagerDutyClientInterceptor(config.getPagerDutyApiKey()));
        return restTemplate;
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory(final NotificationServiceConfig config) {
        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(config.getPagerDutyConnectTimeout())
                .setConnectionRequestTimeout(config.getPagerDutyRequestConnectionTimeout())
                .setSocketTimeout(config.getPagerDutySocketTimeout())
                .build();
        final CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(requestConfig)
                .build();
        return new HttpComponentsClientHttpRequestFactory(client);
    }

    @Bean
    public PagerDutyClient pagerDutyClient(final NotificationServiceConfig config, final RestOperations pagerDutyRestOperations) {
        return new DefaultClient(pagerDutyRestOperations, config.getPagerDutyApiUrl());
    }

    @Bean
    public AlertStore redisAlertStore(final NotificationServiceConfig config) throws URISyntaxException {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(true);
        final JedisPool jedisPool = new JedisPool(poolConfig, new URI(config.getRedisUri()));
        return new RedisAlertStore(jedisPool);
    }

}
