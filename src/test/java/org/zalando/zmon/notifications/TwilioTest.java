package org.zalando.zmon.notifications;

import org.junit.Test;
import org.zalando.zmon.notifications.config.EscalationConfig;
import org.zalando.zmon.notifications.store.PendingNotification;
import org.zalando.zmon.notifications.store.TwilioNotificationStore;

import java.util.ArrayList;
import java.util.Arrays;
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

    @Test
    public void testEscalationOne() {
        EscalationConfig config = new EscalationConfig();
        EscalationConfig.TeamMember luis = new EscalationConfig.TeamMember("Luis", "11111");
        EscalationConfig.TeamMember jan = new EscalationConfig.TeamMember("Jan", "22222");
        EscalationConfig.TeamMember henning = new EscalationConfig.TeamMember("Henning", "33333");
        EscalationConfig.TeamMember marko = new EscalationConfig.TeamMember("Marko", "44444");

        config.getMembers().add(luis);
        config.getMembers().add(jan);
        config.getMembers().add(henning);

        config.getOnCall().add("Luis");
        config.getOnCall().add("Jan");

        config.getPolicy().add(Arrays.asList(jan, henning, luis));
        config.getPolicy().add(Arrays.asList(marko));

        List<String> toCall = TwilioNotificationStore.getNumbersFromTeam(config);
        assertEquals(Arrays.asList("11111","22222","44444"), toCall);
    }
}
