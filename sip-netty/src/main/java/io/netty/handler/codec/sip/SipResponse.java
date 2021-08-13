package io.netty.handler.codec.sip;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public interface SipResponse extends SipMessage{
    SipResponseStatus status();
    SipResponse setStatus(SipResponseStatus status);

    @Override
    SipResponse setProtocolVersion(SipVersion version);
}
