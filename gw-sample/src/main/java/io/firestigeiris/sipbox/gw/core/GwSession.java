package io.firestigeiris.sipbox.gw.core;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * @author firestige
 * @version [version], 2021-08-29
 * @since [version]
 */
public interface GwSession {
    String id();
    Map<String, Object> attributes();
    Mono<Void> invalid();
    Instant getCreateTime();
    Instant getLastAccessTime();
    void setMaxIdleTime();
    Duration getMaxIdleTime();
    default void registerFuture(Sinks.One<Object> future) {
        attributes().put("FURTURE", future);
    }
    default Sinks.One<Object> getFuture() {
        return (Sinks.One<Object>)attributes().get("FUTURE");
    }
}
