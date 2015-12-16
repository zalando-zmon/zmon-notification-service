package org.zalando.zmon.notifications;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zalando.zmon.notifications.oauth.TokenInfoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by jmussler on 12/16/15.
 */
@RestController
@RequestMapping(path="/api/v1/mobile/")
public class MobileController {

    @Autowired
    TokenInfoService tokenInfoService;

    @Autowired
    MobileAPIConfig config;

    public static class AlertHeader {
        public String name;
        public String id;
        public String team;
    }

    @Autowired
    ObjectMapper mapper;

    @RequestMapping(path="alert", method= RequestMethod.GET)
    public ResponseEntity<List<AlertHeader>> getAllAlerts(@RequestHeader(value = "Authorization", required = false) String oauthHeader) {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (!uid.isPresent()) {
            return new ResponseEntity<>((List)null, HttpStatus.UNAUTHORIZED);
        }

        List<AlertHeader> alerts = new ArrayList<>();
        return new ResponseEntity<>(alerts, HttpStatus.OK);
    }

    @RequestMapping(path="alert/{alert_id}", method=RequestMethod.GET)
    public ResponseEntity<JsonNode> getAlertDetails(@PathVariable(value="alert_id") int alertId, @RequestHeader(value = "Authorization", required = false) String oauthHeader)
    {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (!uid.isPresent()) {
            return new ResponseEntity<>((JsonNode)null, HttpStatus.UNAUTHORIZED);
        }

        JsonNode node = mapper.createObjectNode();
        return new ResponseEntity<>( node, HttpStatus.OK);
    }

}
