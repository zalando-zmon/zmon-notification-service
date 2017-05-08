package org.zalando.zmon.notifications.pagerduty.webhook;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class IntegrationHelperTest {
    @Parameters(name = "alertKey={0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"ZMON-12345", 12345, null},
                {"ZMON-FOO", 0, NumberFormatException.class },
                {"BAR", 0, IllegalArgumentException.class },
                {"12345", 0, IllegalArgumentException.class },
                {"", 0, IllegalArgumentException.class },
                {null, 0, IllegalArgumentException.class },
        });
    }

    private String given;
    private int want;
    private Class wantExc;

    public IntegrationHelperTest(String alertKey, int alertId, Class exceptionClass) {
        this.given = alertKey;
        this.want = alertId;
        this.wantExc = exceptionClass;
    }

    @Test
    public void test() {
        try {
            final int got = IntegrationHelper.alertIdFromAlertKey(given);
            assertEquals(got, want);
        } catch (Exception ex) {
            assertThat(ex, Matchers.instanceOf(wantExc));
        }
    }
}