package org.zalando.zmon.notifications.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.zalando.zmon.notifications.api.PublishRequestBody;

import static com.google.common.base.Charsets.UTF_8;

public class JsonHelper {

    private static final ObjectMapper mapper = new ObjectMapper(); // Setup Jackson

    public static StringEntity jsonEntityFor(String deviceToken, PublishRequestBody notification) throws JsonProcessingException {
        ObjectNode request = mapper.createObjectNode();

        request.put("to", deviceToken);
        request.put("content_available", true);
        request.put("priority", "high");
        request.put("time_to_live", 60); // TODO personal setting?

        ObjectNode data = request.putObject("data");
        data.put("alert_id", notification.alertId);
        data.put("entity_id", notification.entityId);

        ObjectNode notify = request.putObject("notification");
        notify.put("title", notification.notification.title);
        notify.put("icon", notification.notification.icon);
        notify.put("body", notification.notification.body);
        notify.put("sound", "default");

        String json = mapper.writeValueAsString(request);
        StringEntity result = new StringEntity(json, UTF_8);
        result.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        return result;
    }
}
