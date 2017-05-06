package org.zalando.zmon.notifications.pagerduty.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class PagerDutyClientInterceptor implements ClientHttpRequestInterceptor {
    private final String authorizationToken;

    public PagerDutyClientInterceptor(String apiKey) {
        this.authorizationToken = String.format("Token token=%s", apiKey);
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().set(HttpHeaders.AUTHORIZATION, authorizationToken);
        return execution.execute(request, body);
    }
}
