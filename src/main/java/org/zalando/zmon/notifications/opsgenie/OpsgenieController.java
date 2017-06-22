package org.zalando.zmon.notifications.opsgenie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;
import org.zalando.zmon.notifications.oauth.TokenInfoService;
import org.zalando.zmon.notifications.opsgenie.action.AlertAction;

import java.util.Optional;


/**
 * Created by mabdelhameed on 21/06/2017.
 */
@RestController
@RequestMapping(path = "/api/v1/opsgenie")
public class OpsgenieController {
    private final Logger log = LoggerFactory.getLogger(OpsgenieController.class);

    private final TokenInfoService tokenInfoService;
    private final ActionHandler actionHandler;

    @Autowired
    public OpsgenieController(final TokenInfoService tokenInfoService,
                              final NotificationServiceConfig notificationServiceConfig,
                              final ActionHandler actionHandler) {
        this.tokenInfoService = tokenInfoService;
        this.actionHandler = actionHandler;
        log.info("Opsgenie mode: dryRun={}", notificationServiceConfig.isDryRun());
    }

    @RequestMapping(path="/webhook", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<Void> handleCallback(@RequestHeader(value = "Authorization", required = true) String oauthHeader, @RequestBody AlertAction action) {
        log.debug("Received Opsgenie Web Hook call");
        final Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (!uid.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        actionHandler.handleAction(action);

        return ResponseEntity.ok().build();
    }
}
