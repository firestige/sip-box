package io.netty.handler.codec.sip;

import io.netty.util.internal.ObjectUtil;

import java.net.InetSocketAddress;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public abstract class DefaultSipMessage extends DefaultSipObject implements SipMessage {
    private static final int HASH_CODE_PRIME = 31;
    private SipVersion version;
    private final SipHeaders headers;
    private InetSocketAddress recipient;

    protected DefaultSipMessage(final SipVersion version) {
        this(version, true, false);
    }

    protected DefaultSipMessage(final SipVersion version, boolean validateHeaders, boolean singleFieldHeaders) {
        this(version, singleFieldHeaders
                ? new CombinedSipHeaders(validateHeaders)
                : new DefaultSipHeaders(validateHeaders));
    }

    protected DefaultSipMessage(final SipVersion version, SipHeaders headers) {
        this.version = version;
        this.headers = headers;
    }

    @Override
    public SipHeaders headers() {
        return headers;
    }

    @Override
    public SipVersion protocolVersion() {
        return version;
    }

    @Override
    public SipMessage setProtocolVersion(SipVersion version) {
        this.version = ObjectUtil.checkNotNull(version, "version");
        return this;
    }

    @Override
    public InetSocketAddress recipient() {
        return recipient;
    }

    @Override
    public SipMessage setRecipient(InetSocketAddress address) {
        this.recipient = address;
        return this;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = HASH_CODE_PRIME * result + headers.hashCode();
        result = HASH_CODE_PRIME * result + version.hashCode();
        result = HASH_CODE_PRIME * result + super.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DefaultSipMessage
                && headers().equals(((DefaultSipMessage) obj).headers())
                && protocolVersion().equals(((DefaultSipMessage) obj).protocolVersion())
                && super.equals(obj);
    }


}
