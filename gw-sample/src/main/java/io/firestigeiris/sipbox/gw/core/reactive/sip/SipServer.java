package io.firestigeiris.sipbox.gw.core.reactive.sip;

import io.firestigeiris.sipbox.gw.core.reactive.AbstractServerAdapter;
import io.firestigeiris.sipbox.gw.core.reactive.GwFilterChain;
import reactor.core.publisher.Flux;

import java.util.concurrent.ExecutorService;

/**
 * @author firestige
 * @version [version], 2021-08-29
 * @since [version]
 */
public class SipServer extends AbstractServerAdapter {
    public SipServer(ExecutorService pool, GwFilterChain chain, int parallel) {
        super(pool, chain, parallel);
    }

    @Override
    protected Flux<byte[]> asFlux() {
        return null;
    }
}
