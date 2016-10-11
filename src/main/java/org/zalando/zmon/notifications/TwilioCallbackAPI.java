package org.zalando.zmon.notifications;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by jmussler on 11.10.16.
 */
@RestController
@RequestMapping(path = "/api/v1/twilio")
public class TwilioCallbackAPI {

    @RequestMapping(method = RequestMethod.GET, produces = "application/xml")
    public String getDummyResult(@RequestParam(name = "id") String id) {
        return "<Response>\n" +
                "        <Say voice=\"woman\">ZMON Alert Active - PLANB ELB DOWN</Say>\n" +
                "        <Gather timeout=\"10\" finishOnKey=\"*\">\n" +
                "          <Say voice=\"woman\">Please enter 1 for ACK and 6 for Resolve.</Say>\n" +
                "        </Gather>\n" +
                "</Response>";
    }
}
