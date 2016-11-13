package org.zalando.zmon.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jmussler on 10.06.16.
 */
@Component
public class HttpEventLogger {

    private final boolean enabled;

    private final String forwardUrl;
    private final Executor executor;

    private NotificationServiceMetrics metrics;
    private final Async async;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(HttpEventLogger.class);

    private static class HttpEvent {
        public Map<String, Object> attributes;

        public Date time;
        public int typeId;

        public HttpEvent(Date time, EventType type, Object[] values) {
            this.time = time;
            this.typeId = type.getId();
            this.attributes = new TreeMap<>();

            for (int i = 0; i < type.getFieldNames().size(); ++i) {
                if (i < values.length) {
                    attributes.put(type.getFieldNames().get(i), values[i]);
                } else {
                    attributes.put(type.getFieldNames().get(i), null);
                }
            }
        }
    }

    public static HttpClient getHttpClient(int socketTimeout, int timeout, int maxConnections) {
        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(timeout).build();
        return HttpClients.custom().setMaxConnPerRoute(maxConnections).setMaxConnTotal(maxConnections).setDefaultRequestConfig(config).build();
    }

    @Autowired
    public HttpEventLogger(NotificationServiceMetrics metrics, NotificationServiceConfig config) {
        this.metrics = metrics;
        enabled = config.isEventlogEnabled();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        if (enabled) {
            forwardUrl = config.getEventlogUrl() + "/api/v1";
            log.info("EventLog enabled: {}", forwardUrl);
            executor = Executor.newInstance(getHttpClient(config.getEventlogSocketTimeout(), config.getEventlogTimeout(), config.getEventlogConnections()));
            ExecutorService threadPool = Executors.newFixedThreadPool(config.getEventlogPoolSize());
            async = Async.newInstance().use(threadPool).use(executor);
        } else {
            log.info("EventLog disabled");
            forwardUrl = null;
            async = null;
            executor = null;
        }
    }

    public void log(EventType type, Object... values) {
        if (!enabled) {
            return;
        }

        try {
            Request request = Request.Post(forwardUrl + "/")
                    .bodyString("[" + mapper.writeValueAsString(new HttpEvent(new Date(), type, values)) + "]", ContentType.APPLICATION_JSON);

            async.execute(request, new FutureCallback<Content>() {

                public void failed(final Exception ex) {
                    metrics.markEventLogError();
                }

                public void completed(final Content content) {
                }

                public void cancelled() {
                }

            });
        } catch (Throwable t) {
            log.error("EventLog write failed: {}", t.getMessage());
            metrics.markEventLogError();
        }
    }
}
