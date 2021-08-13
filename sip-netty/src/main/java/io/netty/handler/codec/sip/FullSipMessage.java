package io.netty.handler.codec.sip;

import io.netty.buffer.ByteBuf;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public interface FullSipMessage extends SipMessage, LastSipContent {
    @Override
    FullSipMessage copy();

    @Override
    FullSipMessage duplicate();

    @Override
    FullSipMessage retainedDuplicate();

    @Override
    FullSipMessage replace(ByteBuf content);

    @Override
    FullSipMessage retain();

    @Override
    FullSipMessage retain(int increment);

    @Override
    FullSipMessage touch();

    @Override
    FullSipMessage touch(Object hint);
}
