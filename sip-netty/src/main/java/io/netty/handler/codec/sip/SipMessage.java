package io.netty.handler.codec.sip;

import java.net.InetSocketAddress;

/**
 * @author firestige
 * @version 0.0.1, 2021-08-15
 * @since 0.0.1
 */
public interface SipMessage extends SipObject {
    SipVersion protocolVersion();
    SipMessage setProtocolVersion(SipVersion version);
    SipHeaders headers();
    InetSocketAddress recipient();
    SipMessage setRecipient(InetSocketAddress address);
}
