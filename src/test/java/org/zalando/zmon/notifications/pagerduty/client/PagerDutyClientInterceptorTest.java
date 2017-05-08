package org.zalando.zmon.notifications.pagerduty.client;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.mock.http.client.MockClientHttpRequest;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PagerDutyClientInterceptorTest {

    private final ClientHttpRequestInterceptor interceptor = new PagerDutyClientInterceptor("test-psk");

    @Test
    public void testInterceptor() throws Exception {
        final HttpRequest request = new MockClientHttpRequest();
        final ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        interceptor.intercept(request, null, execution);
        verify(execution).execute(eq(request), any());
        assertThat(request.getHeaders(), hasKey(HttpHeaders.AUTHORIZATION));
        final List<String> authHeaderValues = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        assertThat(authHeaderValues, hasItem("Token token=test-psk"));
    }
}