package org.zalando.zmon.notifications.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.entity.StringEntity;
import org.junit.Test;
import org.zalando.zmon.notifications.NotificationServiceApplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by hman on 16.12.15.
 */
public class JsonHelperTest {

    @Test
    public void jsonEntityForNotification() throws Exception {
        NotificationServiceApplication.PublishRequestBody publishRequestBody = new NotificationServiceApplication.PublishRequestBody();

        ObjectMapper objectMapper = new ObjectMapper();

        publishRequestBody.alert_id = 42;
        publishRequestBody.data = objectMapper.readTree("{\"entity\" : \"customer4.db.zalando\"}");
        publishRequestBody.notification = objectMapper.readTree("{\"title\" : \"No database connection to master\"}");

        StringEntity stringEntity = JsonHelper.jsonEntityFor(publishRequestBody);
        assertEquals(
                "{\"alert_id\":42,\"data\":{\"entity\":\"customer4.db.zalando\"},\"notification\":{\"title\":\"No database connection to master\"}}",
                stringEntityContent(stringEntity)
        );

        assertEquals("application/json", stringEntity.getContentType().getValue());

    }

    private String stringEntityContent(StringEntity stringEntity) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        stringEntity.writeTo(byteArrayOutputStream);
        return new String(byteArrayOutputStream.toByteArray());
    }
}
