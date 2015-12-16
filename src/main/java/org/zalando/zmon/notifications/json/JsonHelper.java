package org.zalando.zmon.notifications.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.zalando.zmon.notifications.NotificationServiceApplication;

import static com.google.common.base.Charsets.UTF_8;

public class JsonHelper {

    private static final ObjectMapper mapper = new ObjectMapper(); // Setup Jackson

    public static StringEntity jsonEntityFor(NotificationServiceApplication.PublishRequestBody notification) throws JsonProcessingException {
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("alert_id", notification.alert_id);
        objectNode.set("data", notification.data);
        objectNode.set("notification", notification.notification);
        String json = mapper.writeValueAsString(objectNode);
        StringEntity result = new StringEntity(json, UTF_8);
        result.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        return result;
    }
}
