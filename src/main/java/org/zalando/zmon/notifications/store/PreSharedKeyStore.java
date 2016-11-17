package org.zalando.zmon.notifications.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jmussler on 12/16/15.
 */
public class PreSharedKeyStore {

    private final static Logger LOG = LoggerFactory.getLogger(PreSharedKeyStore.class);

    private final Map<String, Long> keys;

    public PreSharedKeyStore(Map<String, Long> keys) {
        if (null == keys) {
            keys = new HashMap<>();
        }

        for (Map.Entry<String, Long> entry : keys.entrySet()) {
            LOG.info("Adding preshared: key={} expires={}s", entry.getKey().substring(0, 4), entry.getValue());
        }

        this.keys = keys;
    }

    public boolean isKeyValid(String key) {
        return keys.containsKey(key)
                && ((System.currentTimeMillis()/1000) < keys.get(key));
    }
}
