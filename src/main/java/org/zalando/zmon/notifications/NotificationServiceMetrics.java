package org.zalando.zmon.notifications;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.springframework.stereotype.Component;

/**
 * Created by jmussler on 14.10.16.
 */
@Component
public class NotificationServiceMetrics {

    private final MetricRegistry metrics;
    private final Meter eventlogErrorMeter;
    private final Meter callsTriggered;
    private final Meter aggregatedCallsTriggered;
    private final Meter entityAckReceived;
    private final Meter alertAckReceived;


    public NotificationServiceMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
        this.eventlogErrorMeter = metrics.meter("notification-service.eventlog-errors");
        this.callsTriggered = metrics.meter("notification-service.calls.triggered");
        this.aggregatedCallsTriggered = metrics.meter("notification-service.calls.aggregate-triggered");
        this.entityAckReceived = metrics.meter("notification-service.ack.entity");
        this.alertAckReceived = metrics.meter("notification-service.ack.alert");
    }

    public void markEventLogError() { eventlogErrorMeter.mark(); }

    public void markCallTriggered() { callsTriggered.mark(); }
    public void markAggregatedCallTriggered() { aggregatedCallsTriggered.mark(); }
    public void markEntityAck() { entityAckReceived.mark(); }
    public void markAlertAck() { alertAckReceived.mark(); }
}
