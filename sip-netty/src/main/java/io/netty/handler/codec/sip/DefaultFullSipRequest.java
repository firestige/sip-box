package io.netty.handler.codec.sip;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.IllegalReferenceCountException;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * @author firestige
 * @version [version], 2021-08-16
 * @since [version]
 */
public class DefaultFullSipRequest extends DefaultSipRequest implements FullSipRequest {
    private final ByteBuf content;
    private final SipHeaders trailingHeader;

    private int hash;

    public DefaultFullSipRequest(SipVersion version,
                                 SipMethod method,
                                 String uri) {
        this(version, method, uri, Unpooled.buffer(0));
    }

    public DefaultFullSipRequest(SipVersion version,
                                 SipMethod method,
                                 String uri,
                                 ByteBuf content) {
        this(version, method,  uri, content, true);
    }

    public DefaultFullSipRequest(SipVersion version,
                                 SipMethod method,
                                 String uri,
                                 boolean validateHeaders){
        this(version, method, uri, Unpooled.buffer(0), validateHeaders);
    }

    public DefaultFullSipRequest(SipVersion version,
                                 SipMethod method,
                                 String uri,
                                 ByteBuf content,
                                 boolean validateHeaders) {
        super(version, method, uri, validateHeaders);
        this.content = checkNotNull(content, "content");
        this.trailingHeader = new DefaultSipHeaders(validateHeaders);
    }

    public DefaultFullSipRequest(SipVersion version,
                                 SipMethod method,
                                 String uri,
                                 ByteBuf content,
                                 SipHeaders headers,
                                 SipHeaders trailingHeader) {
        super(version, method, uri, headers);
        this.content = checkNotNull(content, "content");
        this.trailingHeader = checkNotNull(trailingHeader, "trailingHeader");
    }

    @Override
    public SipHeaders trailingHeaders() {
        return trailingHeader;
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
    public FullSipRequest retain() {
        content.retain();
        return this;
    }

    @Override
    public FullSipRequest retain(int increment) {
        content.retain(increment);
        return this;
    }

    @Override
    public FullSipRequest touch() {
        content.touch();
        return this;
    }

    @Override
    public FullSipRequest touch(Object hint) {
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
    public FullSipRequest setProtocolVersion(SipVersion version) {
        super.setProtocolVersion(version);
        return this;
    }

    @Override
    public FullSipRequest setMethod(SipMethod method) {
        super.setMethod(method);
        return this;
    }

    @Override
    public FullSipRequest setUri(String uri) {
        super.setUri(uri);
        return this;
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
        FullSipRequest request = new DefaultFullSipRequest(
                protocolVersion(),
                method(),
                uri(),
                content,
                headers().copy(),
                trailingHeaders().copy());
        request.setDecoderResult(decoderResult());
        return request;
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
        if (!(obj instanceof DefaultFullSipRequest)) {
            return false;
        }

        DefaultFullSipRequest other = (DefaultFullSipRequest) obj;
        return super.equals(other)
                && content().equals(other.content())
                && trailingHeaders().equals(other.trailingHeaders());
    }

    @Override
    public String toString() {
        return SipMessageUtil.appendFullRequest(new StringBuilder(256), this).toString();
    }
}
