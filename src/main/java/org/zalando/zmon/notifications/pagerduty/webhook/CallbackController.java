package org.zalando.zmon.notifications.pagerduty.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.pagerduty.webhook.domain.Callback;

import java.util.Optional;

@RestController
@RequestMapping(path = "/api/v1/pagerduty")
public class CallbackController {
    private final Logger log = LoggerFactory.getLogger(CallbackController.class);

    private final TokenInfoService tokenInfoService;
    private final CallbackService callbackService;

    @Autowired
    public CallbackController(final TokenInfoService tokenInfoService,
                              final NotificationServiceConfig notificationServiceConfig,
                              final CallbackService callbackService) {
        this.tokenInfoService = tokenInfoService;
        this.callbackService = callbackService;
        log.info("PagerDuty mode: dryRun={}", notificationServiceConfig.isDryRun());
    }

    @RequestMapping(path="/webhook", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Void> handleCallback(@RequestParam(required = false) String psk, @RequestBody Callback callback) {
        log.debug("Received PagerDuty Web Hook call");
        final Optional<String> uid = tokenInfoService.lookupUid(psk);
        if (!uid.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        callbackService.handledMessages(callback.getMessages());

        return ResponseEntity.ok().build();
    }
}
