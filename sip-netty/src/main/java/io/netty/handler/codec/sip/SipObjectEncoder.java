package io.netty.handler.codec.sip;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.internal.StringUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static io.netty.handler.codec.sip.SipConstants.CR;
import static io.netty.handler.codec.sip.SipConstants.LF;

/**
 * @author firestige
 * @version [version], 2021-08-16
 * @since [version]
 */
public abstract class SipObjectEncoder <S extends SipMessage> extends MessageToMessageEncoder<Object> {
    static final int CRLF_SHORT = (CR << 8) | LF;

    private static final float HEADERS_WEIGHT_NEW = 1 / 5f;
    private static final float HEADERS_WEIGHT_HISTORICAL = 1 - HEADERS_WEIGHT_NEW;
    private static final float TRAILERS_WEIGHT_NEW = HEADERS_WEIGHT_NEW;
    private static final float TRAILERS_WEIGHT_HISTORICAL = HEADERS_WEIGHT_HISTORICAL;

    private State currentState = State.INIT;
    private float headersEncodedSizeAccumulator = 256;

    private enum State {
        INIT,
        CONTENT_ALWAYS_EMPTY,
        FIX_CONTENT;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        ByteBuf buf = null;
        if (msg instanceof SipMessage) {
            buf = encodeSipMessage(ctx, (S) msg);
        }
        // 处理可能为空的ByteBuf
        if (msg instanceof ByteBuf) {
            final ByteBuf potentialEmptyBuf = (ByteBuf) msg;
            if (!potentialEmptyBuf.isReadable()) {
                out.add(potentialEmptyBuf.retain());
                return;
            }
        }
        // 序列化body
        if (msg instanceof SipContent || msg instanceof ByteBuf) {
            encodeContent(buf, msg, out);
        }
        Optional.ofNullable(buf).ifPresent(out::add);
    }

    private ByteBuf encodeSipMessage(ChannelHandlerContext ctx, S msg) {
        if (currentState != State.INIT) {
            throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg)
                    + ", state: " + currentState);
        }
        ByteBuf buf = ctx.alloc().buffer((int) headersEncodedSizeAccumulator);
        // 思考要不要捕获异常
        encodeInitialLine(buf, msg);
        currentState = isContentAlwaysEmpty(msg) ? State.CONTENT_ALWAYS_EMPTY : State.FIX_CONTENT;
        sanitizeHeadersBeforeEncode(msg, isContentAlwaysEmpty(msg));
        encodeHeaders(buf, msg.headers());
        ByteBufUtil.writeShortBE(buf, CRLF_SHORT);

        updateHeaderEncodeSize(buf);

        return buf;
    }

    private void updateHeaderEncodeSize(ByteBuf buf) {
        this.headersEncodedSizeAccumulator =
                HEADERS_WEIGHT_NEW * padSizeForAccumulation(buf.readableBytes()) + HEADERS_WEIGHT_HISTORICAL * headersEncodedSizeAccumulator;
    }

    /**
     * 附带一点冗余，这样在追加header的时候减少缓存扩展+复制的开销
     *
     * @param readableBytes buf中可被读取的字节数
     * @return 附加33%额外冗余的{@code readableBytes}
     */
    private static int padSizeForAccumulation(int readableBytes) {
        return (readableBytes << 2) / 3;
    }


    protected abstract void encodeInitialLine(ByteBuf buf, S message) throws RuntimeException;

    protected boolean isContentAlwaysEmpty(S message) {
        return false;
    }

    protected void sanitizeHeadersBeforeEncode(S message, boolean isAlwaysEmpty) {}

    private void encodeHeaders(ByteBuf buf, SipHeaders headers) {
        Iterator<Map.Entry<CharSequence, CharSequence>> iter = headers.iteratorCharSequence();
        while (iter.hasNext()) {
            Entry<CharSequence, CharSequence> header = iter.next();
            SipHeadersEncoder.encoderHeader(header.getKey(), header.getValue(), buf);
        }
    }

    private void encodeContent(ByteBuf buf, Object msg, List<Object> out) {
        switch (currentState) {
            case INIT:
                throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg)
                        + ", state: " + currentState.name());
            case FIX_CONTENT:
                final int contentLength = contentLength(msg);
                if (contentLength > 0) {
                    writeContent(buf, msg, out, contentLength);
                    break;
                }
            case CONTENT_ALWAYS_EMPTY:
                out.add(Optional.ofNullable(buf).orElse(Unpooled.EMPTY_BUFFER));
                break;
            default:
                throw new Error();
        }
        resetToInitIfNeeded(msg);
    }

    private int contentLength(Object msg) {
        if (msg instanceof SipContent) {
            return ((SipContent) msg).content().readableBytes();
        }
        if (msg instanceof ByteBuf) {
            return ((ByteBuf) msg).readableBytes();
        }
        throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg));
    }

    private static Object encodeAndRetain(Object msg) {
        if (msg instanceof SipContent) {
            return ((SipContent) msg).content().retain();
        }
        if (msg instanceof ByteBuf) {
            return ((ByteBuf) msg).retain();
        }
        throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg));
    }

    private void writeContent(ByteBuf buf, Object msg, List<Object>out, int length) {
        if (buf != null && buf.writableBytes() >= length && msg instanceof SipContent) {
            buf.writeBytes(((SipContent) msg).content());
            out.add(buf);
        } else {
            Optional.ofNullable(buf).ifPresent(out::add);
            out.add(encodeAndRetain(msg));
        }
        resetToInitIfNeeded(msg);
    }

    private void resetToInitIfNeeded(Object msg) {
        Optional.ofNullable(msg)
                .filter(LastSipContent.class::isInstance)
                .ifPresent(m -> this.currentState = State.INIT);
    }
}
