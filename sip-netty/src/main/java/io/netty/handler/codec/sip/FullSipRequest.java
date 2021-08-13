package io.netty.handler.codec.sip;

import io.netty.buffer.ByteBuf;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public interface FullSipRequest extends SipRequest, FullSipMessage {
    @Override
    FullSipRequest copy();

    @Override
    FullSipRequest duplicate();

    @Override
    FullSipRequest retainedDuplicate();

    @Override
    FullSipRequest replace(ByteBuf content);

    @Override
    FullSipRequest retain();

    @Override
    FullSipRequest retain(int increment);

    @Override
    FullSipRequest touch();

    @Override
    FullSipRequest touch(Object hint);

    @Override
    FullSipRequest setProtocolVersion(SipVersion version);

    @Override
    FullSipRequest setMethod(SipMethod method);

    @Override
    FullSipRequest setUri(String uri);
}
