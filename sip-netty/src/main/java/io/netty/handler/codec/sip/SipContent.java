package io.netty.handler.codec.sip;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public interface SipContent extends SipObject, ByteBufHolder {
    @Override
    SipContent copy();
    @Override
    SipContent duplicate();
    @Override
    SipContent retainedDuplicate();
    @Override
    SipContent replace(ByteBuf content);
    @Override
    SipContent retain();
    @Override
    SipContent retain(int increment);
    @Override
    SipContent touch();
    @Override
    SipContent touch(Object hint);
}
