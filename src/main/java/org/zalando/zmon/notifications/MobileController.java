package org.zalando.zmon.notifications;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zalando.zmon.notifications.oauth.TokenInfoService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

/**
 * Created by jmussler on 12/16/15.
 */
@RestController
@RequestMapping(path = "/api/v1/mobile/")
public class MobileController {

    @Autowired
    TokenInfoService tokenInfoService;

    @Autowired
    MobileAPIConfig config;

    @Autowired
    ObjectMapper mapper;

    @RequestMapping(path = "alert", method = RequestMethod.GET)
    public ResponseEntity<JsonNode> getAllAlerts(@RequestParam(value = "team", required = false, defaultValue = "*") String team, @RequestHeader(value = "Authorization", required = false) String oauthHeader) throws URISyntaxException, IOException {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (!uid.isPresent()) {
            return new ResponseEntity<>((JsonNode) null, HttpStatus.UNAUTHORIZED);
        }

        URI uri = new URIBuilder().setPath(config.dataServiceUrl + "/api/v1/mobile/alert").addParameter("team", team).build();
        final String r = Request.Get(uri).addHeader("Authorization", oauthHeader).useExpectContinue().execute().returnContent().asString();

        JsonNode node = mapper.readTree(r);
        return new ResponseEntity<>(node, HttpStatus.OK);
    }

    @RequestMapping(path = "alert/{alert_id}", method = RequestMethod.GET)
    public ResponseEntity<JsonNode> getAlertDetails(@PathVariable(value = "alert_id") int alertId, @RequestHeader(value = "Authorization", required = false) String oauthHeader) throws URISyntaxException, IOException {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (!uid.isPresent()) {
            return new ResponseEntity<>((JsonNode) null, HttpStatus.UNAUTHORIZED);
        }

        URI uri = new URIBuilder().setPath(config.dataServiceUrl + "/api/v1/mobile/alert/" + alertId).build();
        final String r = Request.Get(uri).addHeader("Authorization", oauthHeader).useExpectContinue().execute().returnContent().asString();

        JsonNode node = mapper.readTree(r);

        return new ResponseEntity<>(node, HttpStatus.OK);
    }

    @RequestMapping(path = "active-alerts", method = RequestMethod.GET)
    public ResponseEntity<JsonNode> getActiveAlerts(@RequestParam(value = "team", required = false, defaultValue = "*") String team, @RequestHeader(value = "Authorization", required = false) String oauthHeader) throws URISyntaxException, IOException {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (!uid.isPresent()) {
            return new ResponseEntity<>((JsonNode) null, HttpStatus.UNAUTHORIZED);
        }

        URI uri = new URIBuilder().setPath(config.dataServiceUrl + "/api/v1/mobile/active-alerts").addParameter("team", team).build();
        final String r = Request.Get(uri).addHeader("Authorization", oauthHeader).useExpectContinue().execute().returnContent().asString();

        JsonNode node = mapper.readTree(r);
        return new ResponseEntity<>(node, HttpStatus.OK);
    }

    @RequestMapping(path = "all-teams", method = RequestMethod.GET)
    public ResponseEntity<JsonNode> getAllTeams(@RequestHeader(value = "Authorization", required = false) String oauthHeader) throws URISyntaxException, IOException {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (!uid.isPresent()) {
            return new ResponseEntity<>((JsonNode) null, HttpStatus.UNAUTHORIZED);
        }

        URI uri = new URIBuilder().setPath(config.dataServiceUrl + "/api/v1/mobile/all-teams").build();
        final String r = Request.Get(uri).addHeader("Authorization", oauthHeader).useExpectContinue().execute().returnContent().asString();

        JsonNode node = mapper.readTree(r);

        return new ResponseEntity<>(node, HttpStatus.OK);
    }

    @RequestMapping(path = "status", method = RequestMethod.GET)
    public ResponseEntity<JsonNode> getStatus(@RequestHeader(value = "Authorization", required = false) String oauthHeader) throws URISyntaxException, IOException {
        Optional<String> uid = tokenInfoService.lookupUid(oauthHeader);
        if (!uid.isPresent()) {
            return new ResponseEntity<>((JsonNode) null, HttpStatus.UNAUTHORIZED);
        }

        URI uri = new URIBuilder().setPath(config.dataServiceUrl + "/api/v1/mobile/status").build();
        final String r = Request.Get(uri).addHeader("Authorization", oauthHeader).useExpectContinue().execute().returnContent().asString();

        JsonNode node = mapper.readTree(r);

        return new ResponseEntity<>(node, HttpStatus.OK);
    }

}
