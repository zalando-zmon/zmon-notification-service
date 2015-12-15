package org.zalando.zmon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by jmussler on 8/12/15.
 */
public class TokenInfoService {

    private static final Logger LOG = LoggerFactory.getLogger(TokenInfoService.class);

    private final String url;
    private final Executor executor;
    private final ObjectMapper mapper = new ObjectMapper();

    private final LoadingCache<String, Boolean> tokenCache;

    public TokenInfoService(String url) {
        this.url = url;
        this.executor = Executor.newInstance();

        tokenCache = CacheBuilder.newBuilder().expireAfterWrite(600, TimeUnit.SECONDS).build(
                new CacheLoader<String, Boolean>() {
                    @Override
                    public Boolean load(String s) throws Exception {
                        return null;
                    }
                });
    }

    public boolean isValidHeader(String header) {
        if(null == header) return false;
        if("".equals(header)) return false;
        return isValidToken(header.replace("Bearer ", ""));
    }

    public boolean isValidToken(String token) {
        if(null==token) {
            return false;
        }

        if("".equals(token)) {
            return false;
        }

        try {
            Boolean v = tokenCache.get(token);
            if (v != null && v.booleanValue()== true) {
                return true;
            }
        }
        catch(Exception ex) {

        }

        try {
            // throws exception in != 200 status code
            String body = executor.execute(Request.Get(url + token)).returnContent().asString();
            JsonNode result = mapper.readTree(body);
            if(result.has("uid") && result.get("expires_in").asInt()>0) {
                tokenCache.put(token, true);
                return true;
            }

        } catch (IOException e) {
            LOG.error("", e);
        }

        return false;
    }
}
