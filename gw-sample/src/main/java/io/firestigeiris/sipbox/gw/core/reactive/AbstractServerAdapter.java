package io.firestigeiris.sipbox.gw.core.reactive;

import io.firestigeiris.sipbox.gw.core.GwExchange;
import io.firestigeiris.sipbox.gw.core.Server;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ExecutorService;

/**
 * @author firestige
 * @version [version], 2021-08-29
 * @since [version]
 */
@Slf4j
public abstract class AbstractServerAdapter<T> implements Server {
    private final ExecutorService pool;
    private final GwFilterChain chain;
    private final int parallel;
    private final Sinks.Many<GwExchange> sinks;

    protected AbstractServerAdapter(ExecutorService pool, GwFilterChain chain, int parallel) {
        this.pool = pool;
        this.chain = chain;
        this.parallel = parallel;
        this.sinks = Sinks.many().unicast().onBackpressureBuffer();
    }

    @Override
    public void start() {
        init();
    }

    @Override
    public void stop() {

    }

    public Sinks.Many<GwExchange> getSinks() {
        return sinks;
    }

    private void init() {
        sinks.asFlux()
                .parallel(parallel)
                .runOn(Schedulers.fromExecutor(pool))
                .flatMap(chain::filter)
                .subscribe();
    }
}
