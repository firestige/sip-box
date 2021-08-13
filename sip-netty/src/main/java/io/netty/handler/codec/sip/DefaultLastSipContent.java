package io.netty.handler.codec.sip;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DefaultHeaders;
import io.netty.util.internal.StringUtil;

import java.util.Map;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public class DefaultLastSipContent extends DefaultSipContent implements LastSipContent {
    private final SipHeaders trailingHeaders;
    private final boolean validateHeaders;
    public DefaultLastSipContent() {
        this(Unpooled.buffer(0));
    }

    public DefaultLastSipContent(ByteBuf content) {
        this(content, true);
    }
    public DefaultLastSipContent(ByteBuf content, boolean validateHeaders) {
        super(content);
        this.trailingHeaders = new TrailingSipHeaders(validateHeaders);
        this.validateHeaders = validateHeaders;
    }

    @Override
    public LastSipContent copy() {
        return replace(content().copy());
    }

    @Override
    public LastSipContent duplicate() {
        return replace(content().duplicate());
    }

    @Override
    public LastSipContent retainedDuplicate() {
        return replace(content().retainedDuplicate());
    }

    @Override
    public LastSipContent replace(ByteBuf content) {
        final DefaultLastSipContent dup = new DefaultLastSipContent(content, validateHeaders);
        dup.trailingHeaders().set(trailingHeaders());
        return dup;
    }

    @Override
    public LastSipContent retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public LastSipContent retain() {
        super.retain();
        return this;
    }

    @Override
    public LastSipContent touch() {
        super.touch();
        return this;
    }

    @Override
    public LastSipContent touch(Object hint) {
        super.touch(hint);
        return this;
    }

    @Override
    public SipHeaders trailingHeaders() {
        return trailingHeaders;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(super.toString());
        buf.append(StringUtil.NEWLINE);
        appendHeaders(buf);

        buf.setLength(buf.length() - StringUtil.NEWLINE.length());
        return buf.toString();
    }

    private void appendHeaders(StringBuilder buf) {
        for (Map.Entry<String, String> e : trailingHeaders()) {
            buf.append(e.getKey());
            buf.append(": ");
            buf.append(e.getValue());
            buf.append(StringUtil.NEWLINE);
        }
    }

    private static final class TrailingSipHeaders extends DefaultSipHeaders {
        private static final DefaultHeaders.NameValidator<CharSequence> TRAILER_NAME_VALIDATOR = name -> {
            DefaultSipHeaders.SipNameValidator.validateName(name);
            if (SipHeaderNames.CONTENT_LENGTH.contentEqualsIgnoreCase(name)) {
                throw new IllegalArgumentException("prohibited trailing header: " + name);
            }
        };

        TrailingSipHeaders(boolean validate) {
            super(validate, validate ? TRAILER_NAME_VALIDATOR : DefaultHeaders.NameValidator.NOT_NULL);
        }

    }
}
