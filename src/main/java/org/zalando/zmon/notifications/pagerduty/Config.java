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
import org.zalando.zmon.notifications.HttpEventLogger;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.pagerduty.client.DefaultClient;
import org.zalando.zmon.notifications.pagerduty.client.PagerDutyClient;
import org.zalando.zmon.notifications.pagerduty.client.PagerDutyClientInterceptor;
import org.zalando.zmon.notifications.pagerduty.webhook.*;
import org.zalando.zmon.notifications.pagerduty.webhook.handlers.AcknowledgeHandler;
import org.zalando.zmon.notifications.pagerduty.webhook.handlers.ResolveHandler;
import org.zalando.zmon.notifications.pagerduty.webhook.handlers.TriggerHandler;
import org.zalando.zmon.notifications.pagerduty.webhook.handlers.UnacknowledgeHandler;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

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
        final JedisPool jedisPool = new JedisPool(poolConfig, new URI(config.getAlertsRedisUri()));
        return new RedisAlertStore(jedisPool);
    }

    @Bean
    public CallbackService pagerDutyWebHookService(List<MessageHandler> messageHandlers) {
        return new DefaultCallbackService(messageHandlers);
    }

    @Bean
    public MessageHandler triggerHandler(NotificationServiceConfig config, HttpEventLogger eventLog,
                                             PagerDutyClient client) {
        return new TriggerHandler(eventLog, config, client);
    }

    @Bean
    public MessageHandler acknowledgeHandler(NotificationServiceConfig config, HttpEventLogger eventLog,
                                             PagerDutyClient client, AlertStore alertStore) {
        return new AcknowledgeHandler(eventLog, alertStore, config, client);
    }

    @Bean
    public MessageHandler unacknowledgeHandler(NotificationServiceConfig config, HttpEventLogger eventLog,
                                             PagerDutyClient client, AlertStore alertStore) {
        return new UnacknowledgeHandler(config, eventLog, client, alertStore);
    }

    @Bean
    public MessageHandler resolveHandler(NotificationServiceConfig config, HttpEventLogger eventLog,
                                             PagerDutyClient client, AlertStore alertStore) {
        return new ResolveHandler(eventLog, alertStore, config, client);
    }

}
