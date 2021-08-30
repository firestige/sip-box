package io.firestigeiris.sipbox.gw.core.reactive;

import io.firestigeiris.sipbox.gw.core.GwExchange;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * @author firestige
 * @version [version], 2021-08-29
 * @since [version]
 */
public class DefaultGwFilterChain implements GwFilterChain {

    private final List<GwFilter> allFilters;

    private final GwHandler handler;

    @Nullable
    private final GwFilter currentFilter;

    @Nullable
    private final DefaultGwFilterChain next;

    public DefaultGwFilterChain(GwHandler handler, List<GwFilter> filters) {
        Assert.notNull(handler, "GwHandler is required");
        this.allFilters = Collections.unmodifiableList(filters);
        this.handler = handler;
        DefaultGwFilterChain chain = initChain(filters, handler);
        this.currentFilter = chain.currentFilter;
        this.next = chain.next;
    }

    private static DefaultGwFilterChain initChain(List<GwFilter> filters, GwHandler handler) {
        DefaultGwFilterChain chain = new DefaultGwFilterChain(filters, handler, null, null);
        ListIterator<? extends GwFilter> iterator = filters.listIterator(filters.size());
        while (iterator.hasNext()) {
            chain = new DefaultGwFilterChain(filters, handler, iterator.previous(), chain);
        }
        return chain;
    }

    public DefaultGwFilterChain(List<GwFilter> allFilters,
                                GwHandler handler,
                                @Nullable GwFilter currentFilter,
                                @Nullable DefaultGwFilterChain chain) {
        this.allFilters = allFilters;
        this.handler = handler;
        this.currentFilter = currentFilter;
        this.next = chain;
    }

    @Override
    public Mono<Void> filter(GwExchange exchange) {
        return Mono.defer(() -> this.currentFilter != null && this.next != null
                ? invokeFilter(this.currentFilter, this.next, exchange)
                : this.handler.handle(exchange));
    }

    public List<GwFilter> getAllFilters() {
        return allFilters;
    }

    public GwHandler getHandler() {
        return handler;
    }

    private static Mono<Void> invokeFilter(GwFilter current, DefaultGwFilterChain next, GwExchange exchange) {
        String currentName = current.getClass().getName();
        return current.filter(exchange, next).checkpoint(currentName + " [DefaultGwFilterChain]");
    }
}
