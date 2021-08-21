package io.netty.handler.codec.sip;

import io.netty.buffer.ByteBuf;

/**
 * @author firestige
 * @version [version], 2021-08-17
 * @since [version]
 */
public class SipResponseEncoder extends SipObjectEncoder<SipResponse> {
    @Override
    protected void encodeInitialLine(ByteBuf buf, SipResponse message) throws RuntimeException {

    }
}
