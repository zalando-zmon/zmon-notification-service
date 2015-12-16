package org.zalando.zmon.notifications.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import org.apache.http.entity.StringEntity;
import org.zalando.zmon.notifications.NotificationServiceApplication;

public class JsonHelper {

    private static final ObjectMapper mapper = new ObjectMapper(); // Setup Jackson

    public static StringEntity jsonEntityFor(NotificationServiceApplication.PublishRequestBody notification) throws JsonProcessingException {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("alert_id", notification.alert_id);
        objectNode.set("data", notification.data);
        objectNode.set("notification", notification.notification);
        String json = mapper.writeValueAsString(objectNode);
        return new StringEntity(json, Charsets.UTF_8);
    }
}
