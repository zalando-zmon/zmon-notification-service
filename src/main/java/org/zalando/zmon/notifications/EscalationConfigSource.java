package org.zalando.zmon.notifications;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.zalando.zmon.notifications.config.ConfigPayload;
import org.zalando.zmon.notifications.config.EscalationConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jmussler on 07.11.16.
 */
@Service
public class EscalationConfigSource {

    private final Logger log = LoggerFactory.getLogger(EscalationConfigSource.class);

    private final NotificationServiceConfig serviceConfig;

    private final Executor executor;

    private final ObjectMapper mapper;

    private final TokenWrapper token;

    private Map<String, EscalationConfig> escalations = new HashMap<>();

    public static HttpClient getHttpClient(int socketTimeout, int timeout, int maxConnections) {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(timeout).build();
        return HttpClients.custom().setMaxConnPerRoute(maxConnections).setMaxConnTotal(maxConnections).setDefaultRequestConfig(config).build();
    }

    @Autowired
    public EscalationConfigSource(TokenWrapper tokenWrapper, ObjectMapper mapper, NotificationServiceConfig serviceConfig) {
        log.info("Setting up escalation config source: url={}", serviceConfig.getControllerUrl());
        this.token = tokenWrapper;
        this.mapper = mapper;
        this.serviceConfig = serviceConfig;
        executor = Executor.newInstance(getHttpClient(serviceConfig.getControllerSocketTimeout(), serviceConfig.getControllerTimeout(), serviceConfig.getControllerConnections()));
    }

    public EscalationConfig getEscalationConfig(String teamName) {
        final String id = "escalation-team-" + teamName;
        return escalations.get(id);
    }

    @Scheduled(fixedRate = 60000, initialDelay = 15000)
    public void refresh() {
        try {
            URI uri = new URIBuilder(serviceConfig.getControllerUrl() + "/api/v1/entities/").addParameter("query", "{\"type\":\"escalation_config\"}").build();
            Response r = executor.execute(Request.Get(uri).addHeader("Authorization", "Bearer " + token.get()));
            String configString = r.returnContent().asString();
            List<ConfigPayload<EscalationConfig>> escalationWrappers = mapper.readValue(configString, new TypeReference<List<ConfigPayload<EscalationConfig>>>() {});
            HashMap<String, EscalationConfig> map = new HashMap<>();
            StringBuilder b = new StringBuilder();
            for(ConfigPayload<EscalationConfig> e : escalationWrappers) {
                map.put(e.getId().toLowerCase(), e.getData());
                b.append(e.getId().toLowerCase()).append(" ");
            }
            escalations = map;
            log.info("Escalation configs loaded: {}", b);
        }
        catch(Throwable ex) {
            log.error("Failed to load escalation configs", ex);
        }
    }
}
