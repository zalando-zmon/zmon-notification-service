package org.zalando.zmon.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by jmussler on 11.10.16.
 */
@RestController
@RequestMapping(path = "/api/v1/twilio")
public class TwilioCallbackAPI {

    private final Logger log = LoggerFactory.getLogger(TwilioCallbackAPI.class);

    @RequestMapping(method = RequestMethod.POST, produces = "application/xml")
    public String getDummyResult(@RequestParam(name = "id") String id) {
        log.info("Receiving request for id={}", id);
        return "<Response>\n" +
                "        <Say voice=\"woman\">ZMON Alert Active - PLANB ELB DOWN</Say>\n" +
                "        <Gather action=\"/api/v1/twilio/response\" method=\"POST\" numDigits=\"1\" timeout=\"10\" finishOnKey=\"#\">\n" +
                "          <Say voice=\"woman\">Please enter 1 for ACK and 6 for Resolve.</Say>\n" +
                "        </Gather>\n" +
                "</Response>";
    }

    @RequestMapping(path="/response", method = RequestMethod.POST, produces = "application/xml")
    public String ackNotification(@RequestParam Map<String, String> allParams) {
        log.info("Receiving request for params={}", allParams);
        String digits = "1";

        if("1".equals(digits)) {
            return "<Response><Say>Acknowledged</Say></Response>";
        }
        else if("6".equals(digits)) {
            return "<Response><Say>Resolved</Say></Response>";
        }
        else {
            return "<Response><Say>ZMON Response Error</Say></Response>";
        }
    }
}
