package io.netty.handler.codec.sip;

import java.util.Optional;

/**
 * @author firestige
 * @version [version], 2021-08-16
 * @since [version]
 */
public final class SipUtil {
    public static int getContentLength(SipMessage message, int defaultValue) {
        String value = message.headers().get(SipHeaderNames.CONTENT_LENGTH);
        return Optional.ofNullable(value).map(Integer::parseInt).orElse(defaultValue);
    }
}
