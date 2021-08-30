package io.firestigeiris.sipbox.gw.core;

import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author firestige
 * @version [version], 2021-08-29
 * @since [version]
 */
public interface GwExchange {
    GwRequest request();
    GwResponse response();
    Map<String, Object> attributes();
    GwSession session();

    @Nullable
    default <T> T getAttirbute(String name) {
        return (T) attributes().get(name);
    }

    default <T> T getAttributeOrDefault(String name, T defaultValue) {
        return (T) attributes().getOrDefault(name, defaultValue);
    }

    interface Builder {
        Builder request(GwRequest request);
        Builder response(GwResponse response);
        GwExchange build();
    }
}
