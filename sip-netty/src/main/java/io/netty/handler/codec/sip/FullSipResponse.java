package io.netty.handler.codec.sip;

import io.netty.buffer.ByteBuf;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public interface FullSipResponse extends SipResponse, FullSipMessage {
    @Override
    FullSipResponse copy();

    @Override
    FullSipResponse duplicate();

    @Override
    FullSipResponse retainedDuplicate();

    @Override
    FullSipResponse replace(ByteBuf content);

    @Override
    FullSipResponse retain();

    @Override
    FullSipResponse retain(int increment);

    @Override
    FullSipResponse touch();

    @Override
    FullSipResponse touch(Object hint);

    @Override
    FullSipResponse setProtocolVersion(SipVersion version);

    @Override
    FullSipResponse setStatus(SipResponseStatus status);
}
