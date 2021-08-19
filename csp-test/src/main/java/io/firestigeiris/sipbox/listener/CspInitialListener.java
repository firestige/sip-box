package io.firestigeiris.sipbox.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;

/**
 * @author firestige
 * @version [version], 2021-08-18
 * @since [version]
 */
@Slf4j
@Order(3)
public class CspInitialListener implements ApplicationListener<ApplicationStartingEvent> {
    @Override
    public void onApplicationEvent(ApplicationStartingEvent applicationStartingEvent) {
        System.out.println("init csp");
        //log.info("csp init");
    }
}
