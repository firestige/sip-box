package io.firestigeiris.sipbox;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author firestige
 * @version [version], 2021-08-21
 * @since [version]
 */
public class Application {
    public static void main(String[] args) throws InterruptedException {
        SipServer server = new SipServer(18090);
        SipClient client = new SipClient("127.0.0.1", 18090);
        ExecutorService pool = Executors.newCachedThreadPool();
        pool.submit(() -> {
            try {
                server.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        TimeUnit.SECONDS.sleep(1);
        pool.submit(() -> {
            try {
                client.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        TimeUnit.SECONDS.sleep(10);
        System.out.println("done");
    }
}
