package org.zalando.zmon.notifications.oauth;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Optional;

public class GoogleIntegrationTest {

    @Ignore
    @Test
    public void testName() throws Exception {
        OAuthTokenInfoService s = new OAuthTokenInfoService("https://auth.zalando.com/oauth2/tokeninfo?access_token=");

        // enter here a valid token uuid for testing purposes
        Optional<String> s1 = s.lookupUid("Bearer 4c1d78f7-3661-4059-a47f-0325ec674438");
        System.out.println(s1);
    }
}
