package io.firestigeiris.sipbox.gw.core.reactive;

import io.firestigeiris.sipbox.gw.core.GwExchange;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author firestige
 * @version [version], 2021-08-29
 * @since [version]
 */
public class DispatcherGwHandler implements GwHandler, ApplicationContextAware {

    private List<HandlerMapper> handlerMappers;

    private List<HandlerAdapter> handlerAdapters;

    private List<HandlerResultHandler> resultHandlers;

    public DispatcherGwHandler() { }

    @Override
    public Mono<Void> handle(GwExchange exchange) {
        if (exchange.session().getFuture() != null) {
            exchange.session().getFuture().tryEmitValue(exchange);
        } else {

        }
        return Mono.empty();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }
}
