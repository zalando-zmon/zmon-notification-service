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

    public static StringEntity jsonEntityFor(String deviceToken, NotificationServiceApplication.PublishRequestBody notification) throws JsonProcessingException {
       ObjectNode request = mapper.createObjectNode();

        ObjectNode data = request.putObject("data");
        data.put("alert_id", notification.alert_id);
        data.set("data", notification.data);
        data.set("notification", notification.notification);
        request.put("to", deviceToken);

        String json = mapper.writeValueAsString(request);
        StringEntity result = new StringEntity(json, UTF_8);
        result.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        return result;
    }
}
