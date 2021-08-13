package io.netty.handler.codec.sip;

import io.netty.util.internal.ObjectUtil;

/**
 * @author firestige
 * @version [version], 2021-08-16
 * @since [version]
 */
public class DefaultSipRequest extends DefaultSipMessage implements SipRequest {
    private static final int HASH_CODE_PRIME = 31;
    private SipMethod method;
    private String uri;

    public DefaultSipRequest(SipVersion version, SipMethod method, String uri) {
        this(version, method, uri, true);
    }

    public DefaultSipRequest(SipVersion version, SipMethod method, String uri, boolean validateHeaders) {
        super(version, validateHeaders, false);
        this.method = ObjectUtil.checkNotNull(method, "method");
        this.uri = ObjectUtil.checkNotNull(uri, "uri");
    }

    public DefaultSipRequest(SipVersion version, SipMethod method, String uri, SipHeaders headers) {
        super(version, headers);
        this.method = ObjectUtil.checkNotNull(method, "method");
        this.uri = ObjectUtil.checkNotNull(uri, "uri");
    }

    @Override
    public SipMethod method() {
        return method;
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public SipRequest setMethod(SipMethod method) {
        this.method = ObjectUtil.checkNotNull(method, "method");
        return this;
    }

    @Override
    public SipRequest setUri(String uri) {
        this.uri = ObjectUtil.checkNotNull(uri, "uri");
        return this;
    }

    @Override
    public SipRequest setProtocolVersion(SipVersion version) {
        super.setProtocolVersion(version);
        return this;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = HASH_CODE_PRIME * result + method.hashCode();
        result = HASH_CODE_PRIME * result + uri.hashCode();
        result = HASH_CODE_PRIME * result + super.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DefaultSipRequest)) {
            return false;
        }
        DefaultSipRequest other = (DefaultSipRequest) obj;

        return method().equals(other.method()) && uri().equalsIgnoreCase(other.uri()) && super.equals(other);
    }

    @Override
    public String toString() {
        return SipMessageUtil.appendRequest(new StringBuilder(256), this).toString();
    }
}
