package io.netty.handler.codec.sip;

import java.util.Optional;

/**
 * @author firestige
 * @version [version], 2021-08-16
 * @since [version]
 */
public final class SipUtil {
    /**
     * 查询报文长度
     *
     * @param message SIP消息
     * @param defaultValue 默认值
     * @return 报文长度
     */
    public static int getContentLength(SipMessage message, int defaultValue) {
        String value = message.headers().get(SipHeaderNames.CONTENT_LENGTH);
        return Optional.ofNullable(value).map(Integer::parseInt).orElse(defaultValue);
    }

    /**
     * 查询报文是否有设置报文长度
     *
     * @param message SIP消息
     * @return {@code true}有长度，{@code false}没有长度
     */
    public static boolean isContentLengthSet(FullSipMessage message) {
        return message.headers().contains(SipHeaderNames.CONTENT_LENGTH);
    }
}
