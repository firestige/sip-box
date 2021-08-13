package io.netty.handler.codec.sip;

import io.netty.util.internal.ObjectUtil;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public class DefaultSipResponse extends DefaultSipMessage implements SipResponse {

    private SipResponseStatus status;

    public DefaultSipResponse(SipVersion version, SipResponseStatus status) {
        this(version, status, true, false);
    }

    public DefaultSipResponse(SipVersion version, SipResponseStatus status, boolean validateHeaders) {
        this(version, status, validateHeaders, false);
    }

    public DefaultSipResponse(SipVersion version, SipResponseStatus status, boolean validateHeaders,
                              boolean singleFieldHeaders) {
        super(version, validateHeaders, singleFieldHeaders);
        this.status = ObjectUtil.checkNotNull(status, "status");
    }

    public DefaultSipResponse(SipVersion version, SipResponseStatus status, SipHeaders headers) {
        super(version, headers);
        this.status = ObjectUtil.checkNotNull(status, "status");
    }

    @Override
    public SipResponseStatus status() {
        return status;
    }

    @Override
    public SipResponse setStatus(SipResponseStatus status) {
        this.status = ObjectUtil.checkNotNull(status, "status");
        return this;
    }

    @Override
    public SipResponse setProtocolVersion(SipVersion version) {
        super.setProtocolVersion(version);
        return this;
    }

    @Override
    public String toString() {
        return SipMessageUtil.appendResponse(new StringBuilder(256), this).toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + status.hashCode();
        result = 31 * result + super.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DefaultSipResponse
                && status.equals(((DefaultSipResponse) obj).status())
                && super.equals(obj);
    }
}
