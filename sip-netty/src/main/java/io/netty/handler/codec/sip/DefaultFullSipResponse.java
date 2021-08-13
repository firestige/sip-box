package io.netty.handler.codec.sip;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.internal.ObjectUtil;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public class DefaultFullSipResponse extends DefaultSipResponse implements FullSipResponse {
    private final ByteBuf content;
    private final SipHeaders trailingHeaders;

    private int hash;

    public DefaultFullSipResponse(SipVersion version, SipResponseStatus status) {
        this(version, status, Unpooled.buffer(0));
    }

    public DefaultFullSipResponse(SipVersion version,
                                  SipResponseStatus status,
                                  ByteBuf content) {
        this(version, status, content, true);
    }

    public DefaultFullSipResponse(SipVersion version,
                                  SipResponseStatus status,
                                  boolean validateHeaders) {
        this(version, status, Unpooled.buffer(0), validateHeaders);
    }

    public DefaultFullSipResponse(SipVersion version,
                                  SipResponseStatus status,
                                  boolean validateHeaders,
                                  boolean singleFieldHeaders) {
        this(version, status, Unpooled.buffer(0), validateHeaders, singleFieldHeaders);
    }
    public DefaultFullSipResponse(SipVersion version,
                                  SipResponseStatus status,
                                  ByteBuf content,
                                  boolean validateHeaders) {
        this(version, status, content, validateHeaders, false);
    }

    public DefaultFullSipResponse(SipVersion version,
                                  SipResponseStatus status,
                                  ByteBuf content,
                                  boolean validateHeaders,
                                  boolean singleFieldHeaders) {
        super(version, status, validateHeaders, singleFieldHeaders);
        this.content = ObjectUtil.checkNotNull(content, "content");
        this.trailingHeaders = singleFieldHeaders ? new CombinedSipHeaders(validateHeaders)
                                                  : new DefaultSipHeaders(validateHeaders);
    }

    public DefaultFullSipResponse(SipVersion version,
                                  SipResponseStatus status,
                                  ByteBuf content,
                                  SipHeaders headers,
                                  SipHeaders trailingHeaders) {
        super(version, status, headers);
        this.content = content;
        this.trailingHeaders = trailingHeaders;
    }

    @Override
    public SipHeaders trailingHeaders() {
        return trailingHeaders;
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
    public FullSipResponse retain() {
        content.retain();
        return this;
    }

    @Override
    public FullSipResponse retain(int increment) {
        content.retain(increment);
        return this;
    }

    @Override
    public FullSipResponse touch() {
        content.touch();
        return this;
    }

    @Override
    public FullSipResponse touch(Object hint) {
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

    @Override
    public FullSipResponse setProtocolVersion(SipVersion version) {
        super.setProtocolVersion(version);
        return this;
    }

    @Override
    public FullSipResponse setStatus(SipResponseStatus status) {
        super.setStatus(status);
        return this;
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
        FullSipResponse response = new DefaultFullSipResponse(
                protocolVersion(),
                status(),
                content,
                headers().copy(),
                trailingHeaders().copy());
        response.setDecoderResult(decoderResult());
        return response;
    }

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash == 0) {
            if (ByteBufUtil.isAccessible(content())) {
                try {
                    hash = 31 + content().hashCode();
                } catch (IllegalReferenceCountException ignored) {
                    // Handle race condition between checking refCnt() == 0 and using the object.
                    hash = 31;
                }
            } else {
                hash = 31;
            }
            hash = 31 * hash + trailingHeaders().hashCode();
            hash = 31 * hash + super.hashCode();
            this.hash = hash;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DefaultFullSipResponse
                && super.equals(obj)
                && content().equals(((DefaultFullSipResponse) obj).content())
                && trailingHeaders().equals(((DefaultFullSipResponse) obj).trailingHeaders());
    }

    @Override
    public String toString() {
        return SipMessageUtil.appendFullResponse(new StringBuilder(256), this).toString();
    }
}
