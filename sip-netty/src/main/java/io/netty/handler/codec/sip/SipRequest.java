package io.netty.handler.codec.sip;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public interface SipRequest extends SipMessage {
    SipMethod method();
    SipRequest setMethod(SipMethod method);
    String uri();
    SipRequest setUri(String uri);

    @Override
    SipRequest setProtocolVersion(SipVersion version);
}
