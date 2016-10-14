package org.zalando.zmon.notifications;

import org.junit.Test;
import org.zalando.zmon.notifications.store.PendingNotification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by jmussler on 14.10.16.
 */
public class TwilioTest {

    @Test
    public void testTransform() {
        List<PendingNotification> results = new ArrayList<>();
        results.add(new PendingNotification(1, 0, "i1", "entity1", "","", ""));
        results.add(new PendingNotification(1, 0, "i1", "entity2", "","", ""));
        results.add(new PendingNotification(1, 0, "i2", "entity2", "","", ""));
        results.add(new PendingNotification(2, 0, "i3", "entity3", "","", ""));

        Map<Integer, Map<String, List<PendingNotification>>> groups = TwilioCallbackAPI.transformResult(results);
        assertEquals("", 2, groups.size());
        assertEquals("", 2, groups.get(1).size());
        assertEquals("", 1, groups.get(2).size());
        assertEquals("", 2, groups.get(1).get("i1").size());
        assertEquals("", 1, groups.get(1).get("i2").size());
    }
}
