package org.zalando.zmon.notifications.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.tokens.AccessTokenConfiguration;
import org.zalando.stups.tokens.AccessTokens;
import org.zalando.stups.tokens.Tokens;
import org.zalando.zmon.notifications.TokenWrapper;
import org.zalando.zmon.notifications.config.NotificationServiceConfig;

import java.net.URI;

/**
 * Created by jmussler on 07.11.16.
 */
@Configuration
public class Oauth2Config {

    /*
    * In case where no oauth2 infrastructure is used, we always use/send the provided static token
    * */
    @Bean
    public TokenWrapper accessTokens(NotificationServiceConfig config) {
        if (config.getOauth2AccessTokenUrl() == null) {
            return new TokenWrapper(config.getOauth2StaticToken());
        } else {
            AccessTokenConfiguration tokenConfig = Tokens.createAccessTokensWithUri(URI.create(config.getOauth2AccessTokenUrl()))
                    .manageToken("zmon-read");

            for (String scope : config.getOauth2Scopes()) {
                tokenConfig.addScope(scope);
            }

            AccessTokens tokens = tokenConfig.done().start();

            return new TokenWrapper(tokens, "zmon-read");
        }
    }
}
