package org.zalando.zmon.notifications.pagerduty.webhook;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Message;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class CallbackControllerTest {
    @Mock
    private TokenInfoService tokenInfoService;
    @Mock
    private CallbackService callbackService;
    @Mock
    private NotificationServiceConfig config;

    private MockMvc mockMvc;
    private Object webHookController;
    @Before
    public void setUp() {
        webHookController = new CallbackController(tokenInfoService, config, callbackService);
        mockMvc = MockMvcBuilders.standaloneSetup(webHookController)
                .setHandlerExceptionResolvers(getSimpleMappingExceptionResolver()).alwaysDo(print()).build();
    }

    @Test
    public void testValidPayload() throws Exception {
        when(tokenInfoService.lookupUid(anyString())).thenReturn(Optional.of("test-psk"));
        mockMvc.perform(post("/api/v1/pagerduty/webhook")
                .param("psk", "test-psk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testPayload()))
                .andExpect(status().isOk());
        verify(callbackService).handledMessages(anyListOf(Message.class));
    }

    @Test
    public void testMissingAuth() throws Exception {
        when(tokenInfoService.lookupUid(anyString())).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/v1/pagerduty/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testPayload()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testInvalidPayload() throws Exception {
        when(tokenInfoService.lookupUid(anyString())).thenReturn(Optional.of("test-psk"));
        mockMvc.perform(post("/api/v1/pagerduty/webhook")
                .param("psk", "test-psk")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{broken}"))
                .andExpect(status().is4xxClientError());
        verify(callbackService, never()).handledMessages(anyListOf(Message.class));
    }

    @Test
    public void testServiceError() throws Exception {
        when(tokenInfoService.lookupUid(anyString())).thenReturn(Optional.of("test-psk"));
        doThrow(new PagerDutyWebHookException("fail")).when(callbackService).handledMessages(anyListOf(Message.class));
        mockMvc.perform(post("/api/v1/pagerduty/webhook")
                .param("psk", "test-psk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(testPayload()))
                .andExpect(status().isInternalServerError());
    }

    private String testPayload() throws IOException {
        final URL url = this.getClass().getResource("/pagerduty_webhook.json");
        return Resources.toString(url, Charsets.UTF_8);
    }

    private SimpleMappingExceptionResolver getSimpleMappingExceptionResolver() {
        final SimpleMappingExceptionResolver result = new SimpleMappingExceptionResolver();
        final Properties p = new Properties();
        p.put(PagerDutyWebHookException.class.getName(), "Errors/PagerDutyWebHookException");
        p.put(HttpMessageNotReadableException.class.getName(), "Errors/InvalidPayload");
        result.setExceptionMappings(p);
        result.addStatusCode("Errors/PagerDutyWebHookException", HttpStatus.INTERNAL_SERVER_ERROR.value());
        result.addStatusCode("Errors/InvalidPayload", HttpStatus.BAD_REQUEST.value());
        return result;

    }
}