package io.netty.handler.codec.sip;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;
import java.util.function.IntFunction;

import static io.netty.handler.codec.sip.SipConstants.CR;
import static io.netty.handler.codec.sip.SipConstants.LF;

/**
 * @author firestige
 * @version [version], 2021-08-16
 * @since [version]
 */
public abstract class SipObjectEncoder <S extends SipMessage> extends MessageToMessageEncoder<Object> {

    private State state;
    private static final int CRLF_Short = (CR << 8) | LF;

    private enum State {
        INIT,
        CONTENT_ALWAYS_EMPTY;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        ByteBuf buf = null;
        // 确认状态
        if (state != State.INIT) {
            throw new IllegalStateException();
        }
        // 序列化首行
        S m = (S) msg;
        buf = ctx.alloc().buffer(256);
        encodeInitialLine(buf, m);
        // 序列化头域
        encodeHeaders(buf, m);
        ByteBufUtil.writeShortBE(buf, CRLF_Short);
        // 序列化body
        if (!isContentAlwaysEmpty(m)) {
            encodeContent(buf, m);
        }
    }

    private void encodeInitialLine(ByteBuf buf, S m) {

    }

    private void encodeHeaders(ByteBuf buf, S m) {
    }

    protected boolean isContentAlwaysEmpty(S m) {
        return false;
    }

    private void encodeContent(ByteBuf buf, S m) {
    }
}
