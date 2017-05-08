package org.zalando.zmon.notifications.pagerduty.client;

import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

class AbstractResilientClient {
    private final Logger log = LoggerFactory.getLogger(AbstractResilientClient.class);

    private RetryPolicy retryPolicy;
    private CircuitBreaker circuitBreaker;

    AbstractResilientClient() {
        this.retryPolicy = new RetryPolicy()
                .retryOn(this::isTransientFailure)
                .withBackoff(1, 3, TimeUnit.SECONDS)
                .withJitter(0.1)
                .withMaxRetries(3);
        this.circuitBreaker = new CircuitBreaker()
                .withFailureThreshold(3, 10)
                .withSuccessThreshold(3)
                .withDelay(1, TimeUnit.MINUTES)
                .onOpen(() -> log.info("The PagerDuty circuit breaker was opened"));
    }

    private boolean isTransientFailure(Throwable failure) {
        return failure instanceof IOException || failure instanceof HttpServerErrorException;
    }

    <T> T doResilientCall(final Callable<T> callable) {
        return Failsafe.with(circuitBreaker)
                .with(retryPolicy)
                .onFailedAttempt(failure -> log.error("PagerDuty API call failed", failure))
                .onRetry((c, f, ctx) -> log.warn("Failure #{}. Retrying.", ctx.getExecutions()))
                .get(callable);
    }
}
