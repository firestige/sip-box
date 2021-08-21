package io.netty.handler.codec.sip;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.MessageAggregator;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.Optional;

import static io.netty.handler.codec.sip.SipHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.sip.SipUtil.getContentLength;

/**
 * @author firestige
 * @version [version], 2021-08-16
 * @since [version]
 */
public class SipObjectAggregator extends MessageAggregator<SipObject, SipMessage, SipContent, FullSipMessage> {
    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(SipObjectAggregator.class);
    private static final FullSipResponse TOO_LARGE = new DefaultFullSipResponse(
            SipVersion.SIP_2_0,
            SipResponseStatus.REQUEST_ENTITY_TOO_LARGE,
            Unpooled.EMPTY_BUFFER);
    static {
        TOO_LARGE.headers().set(CONTENT_LENGTH, 0);
    }

    public SipObjectAggregator(int maxContentLength) {
        super(maxContentLength);
    }

    @Override
    protected boolean isStartMessage(SipObject msg) {
        return msg instanceof SipMessage;
    }

    @Override
    protected boolean isContentMessage(SipObject msg) {
        return msg instanceof SipContent;
    }

    @Override
    protected boolean isLastContentMessage(SipContent msg) {
        return msg instanceof LastSipContent;
    }

    @Override
    protected boolean isAggregated(SipObject msg) {
        return msg instanceof FullSipMessage;
    }

    @Override
    protected boolean isContentLengthInvalid(SipMessage start, int maxContentLength) {
        try{
            return getContentLength(start, -1) > maxContentLength;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    /**
     * SIP消息没有CONTINUE，不需要实现这个方法，只要返回{@code null}即可
     */
    @Override
    protected Object newContinueResponse(SipMessage start, int maxContentLength, ChannelPipeline pipeline) {
        return null;
    }

    /**
     * SIP消息没有CONTINUE，不需要实现这个方法，只要返回{@code false}即可
     */
    @Override
    protected boolean closeAfterContinueResponse(Object msg) throws Exception {
        return false;
    }

    /**
     * SIP消息没有CONTINUE，不需要实现这个方法，只要返回{@code false}即可
     */
    @Override
    protected boolean ignoreContentAfterContinueResponse(Object msg) throws Exception {
        return false;
    }

    /**
     * 开始拼接消息
     *
     * @param start 起始消息，一般这代表着包含消息头的第一个消息
     * @param content 消息内容
     * @return 完整的Sip消息
     */
    @Override
    protected FullSipMessage beginAggregation(SipMessage start, ByteBuf content) {
        assert !(start instanceof FullSipMessage);

        AggregatedFullSipMessage ret;
        if (start instanceof SipRequest) {
            ret = new AggregatedFullSipRequest((SipRequest) start, content, null);
        } else if (start instanceof SipResponse) {
            ret = new AggregatedFullSipResponse((SipResponse) start, content, null);
        } else {
            throw new Error();
        }
        return ret;
    }

    @Override
    protected void aggregate(FullSipMessage aggregated, SipContent content) {
        if (content instanceof LastSipContent) {
            ((AggregatedFullSipMessage) aggregated).setTrailingHeaders(((LastSipContent) content).trailingHeaders());
        }
    }

    @Override
    protected void finishAggregation(FullSipMessage aggregated) throws Exception {
        if (!SipUtil.isContentLengthSet(aggregated)) {
            aggregated.headers().set(CONTENT_LENGTH, String.valueOf(aggregated.content().readableBytes()));
        }
    }

    @Override
    protected void handleOversizedMessage(ChannelHandlerContext ctx, SipMessage oversized) throws Exception {
        if (oversized instanceof SipRequest) {
            ctx.writeAndFlush(TOO_LARGE.retainedDuplicate()).addListener(future -> {
                if (!future.isSuccess()) {
                    LOGGER.debug("Failed to send a 413 Request Entity Too Large.", future.cause());
                    ctx.close();
                }
            });
        } else if (oversized instanceof SipResponse) {
            ctx.close();
            throw new TooLongFrameException("Response entity too large: " + oversized);
        } else {
            throw new IllegalStateException();
        }
    }

    private abstract static class AggregatedFullSipMessage implements FullSipMessage {
        protected final SipMessage message;
        private final ByteBuf content;
        private SipHeaders trailingHeaders;

        AggregatedFullSipMessage(SipMessage message, ByteBuf content, SipHeaders trailingHeaders) {
            this.message = message;
            this.content = content;
            this.trailingHeaders = trailingHeaders;
        }

        @Override
        public SipHeaders trailingHeaders() {
            return Optional.ofNullable(trailingHeaders).orElse(EmptySipHeaders.INSTANCE);
        }

        void setTrailingHeaders(SipHeaders trailingHeaders) {
            this.trailingHeaders = trailingHeaders;
        }

        @Override
        public SipVersion protocolVersion() {
            return message.protocolVersion();
        }

        @Override
        public SipMessage setProtocolVersion(SipVersion version) {
            message.setProtocolVersion(version);
            return this;
        }

        @Override
        public SipHeaders headers() {
            return message.headers();
        }

        @Override
        public DecoderResult decoderResult() {
            return message.decoderResult();
        }

        @Override
        public void setDecoderResult(DecoderResult result) {
            message.setDecoderResult(result);
        }

        @Override
        public ByteBuf content() {
            return content;
        }

        @Override
        public int refCnt() {
            return content.refCnt();
        }

        @Override
        public FullSipMessage retain() {
            content.retain();
            return this;
        }

        @Override
        public FullSipMessage retain(int increment) {
            content.retain(increment);
            return this;
        }

        @Override
        public FullSipMessage touch() {
            content.touch();
            return this;
        }

        @Override
        public FullSipMessage touch(Object hint) {
            content.touch(hint);
            return this;
        }

        @Override
        public boolean release() {
            return content.release();
        }

        @Override
        public boolean release(int decrement) {
            return content.release(decrement);
        }

    }

    private static final class AggregatedFullSipRequest extends AggregatedFullSipMessage implements FullSipRequest {

        AggregatedFullSipRequest(SipRequest request, ByteBuf content, SipHeaders trailingHeaders) {
            super(request, content, trailingHeaders);
        }

        @Override
        public FullSipRequest copy() {
            return replace(content().copy());
        }

        @Override
        public FullSipRequest duplicate() {
            return replace(content().duplicate());
        }

        @Override
        public FullSipRequest retainedDuplicate() {
            return replace(content().retainedDuplicate());
        }

        @Override
        public FullSipRequest replace(ByteBuf content) {
            DefaultFullSipRequest dup = new DefaultFullSipRequest(
                    protocolVersion(),
                    method(),
                    uri(),
                    content,
                    headers().copy(),
                    trailingHeaders().copy());
            dup.setDecoderResult(decoderResult());
            return dup;
        }

        @Override
        public FullSipRequest retain() {
            super.retain();
            return this;
        }

        @Override
        public FullSipRequest retain(int increment) {
            super.retain(increment);
            return this;
        }

        @Override
        public FullSipRequest touch() {
            super.touch();
            return this;
        }

        @Override
        public FullSipRequest touch(Object hint) {
            super.touch(hint);
            return this;
        }

        @Override
        public SipMethod method() {
            return ((SipRequest)message).method();
        }

        @Override
        public FullSipRequest setMethod(SipMethod method) {
            ((SipRequest)message).setMethod(method);
            return this;
        }

        @Override
        public String uri() {
            return ((SipRequest)message).uri();
        }

        @Override
        public FullSipRequest setUri(String uri) {
            ((SipRequest)message).setUri(uri);
            return this;
        }

        @Override
        public FullSipRequest setProtocolVersion(SipVersion version) {
            super.setProtocolVersion(version);
            return this;
        }

        @Override
        public String toString() {
            return SipMessageUtil.appendFullRequest(new StringBuilder(256), this).toString();
        }
    }

    private static final class AggregatedFullSipResponse extends AggregatedFullSipMessage implements FullSipResponse {
        AggregatedFullSipResponse(SipResponse response, ByteBuf content, SipHeaders trailingHeaders) {
            super(response, content, trailingHeaders);
        }

        @Override
        public FullSipResponse copy() {
            return replace(content().copy());
        }

        @Override
        public FullSipResponse duplicate() {
            return replace(content().duplicate());
        }

        @Override
        public FullSipResponse retainedDuplicate() {
            return replace(content().retainedDuplicate());
        }

        @Override
        public FullSipResponse replace(ByteBuf content) {
            DefaultFullSipResponse dup = new DefaultFullSipResponse(protocolVersion(), status(), content,
                    headers().copy(), headers().copy());
            dup.setDecoderResult(decoderResult());
            return dup;
        }

        @Override
        public SipResponseStatus status() {
            return ((SipResponse) message).status();
        }

        @Override
        public FullSipResponse setStatus(SipResponseStatus status) {
            ((SipResponse) message).setStatus(status);
            return this;
        }

        @Override
        public FullSipResponse setProtocolVersion(SipVersion version) {
            super.setProtocolVersion(version);
            return this;
        }

        @Override
        public FullSipResponse retain() {
            super.retain();
            return this;
        }

        @Override
        public FullSipResponse retain(int increment) {
            super.retain(increment);
            return this;
        }

        @Override
        public FullSipResponse touch() {
            super.touch();
            return this;
        }

        @Override
        public FullSipResponse touch(Object hint) {
            super.touch(hint);
            return this;
        }

        @Override
        public String toString() {
            return SipMessageUtil.appendFullResponse(new StringBuilder(256), this).toString();
        }
    }
}
