package io.firestigeiris.sipbox.gw.core;

import reactor.core.publisher.Mono;

/**
 * @author firestige
 * @version [version], 2021-08-29
 * @since [version]
 */
public interface Server {
    void start();
    void stop();
}
