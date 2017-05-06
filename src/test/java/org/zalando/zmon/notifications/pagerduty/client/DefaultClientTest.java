package org.zalando.zmon.notifications.pagerduty.client;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestOperations;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.zalando.zmon.notifications.pagerduty.client.ClientTestUtils.mockAlert;

public class DefaultClientTest {
    @Test
    public void testGettingAlerts() throws Exception {
        final RestOperations restOperationsMock = mock(RestOperations.class);
        when(restOperationsMock.getForObject(anyString(), Mockito.eq(AlertsResponse.class)))
                .thenReturn(mockAlertsResponse("alert1", "alert2"));

        final PagerDutyClient client = new DefaultClient(restOperationsMock);
        final List<Alert> alerts = client.getAlerts("foo");

        assertThat(alerts, hasSize(2));
    }

    @Test(expected = HttpServerErrorException.class)
    public void testGettingAlertsFailures() throws Exception {
        final RestOperations restOperationsMock = mock(RestOperations.class);
        when(restOperationsMock.getForObject(anyString(), Mockito.eq(AlertsResponse.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        final PagerDutyClient client = new DefaultClient(restOperationsMock);
        client.getAlerts("bar");
    }

    private AlertsResponse mockAlertsResponse(String ... keys) {
        final AlertsResponse response = new AlertsResponse();
        final ImmutableList.Builder<Alert> builder = ImmutableList.builder();
        for (final String key : keys) {
            builder.add(mockAlert(key));
        }
        response.setAlerts(builder.build());
        return response;
    }
}