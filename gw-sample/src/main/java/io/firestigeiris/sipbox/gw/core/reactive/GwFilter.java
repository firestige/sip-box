package io.firestigeiris.sipbox.gw.core.reactive;

import io.firestigeiris.sipbox.gw.core.GwExchange;
import reactor.core.publisher.Mono;

/**
 * @author firestige
 * @version [version], 2021-08-29
 * @since [version]
 */
public interface GwFilter {
    Mono<Void> filter(GwExchange exchange, GwFilterChain chain);
}
