package io.netty.handler.codec.sip;

/**
 *    A valid SIP request formulated by a UAC MUST, at a minimum, contain
 *    the following header fields: To, From, CSeq, Call-ID, Max-Forwards,
 *    and Via; all of these header fields are mandatory in all SIP
 *    requests.  These six header fields are the fundamental building
 *    blocks of a SIP message, as they jointly provide for most of the
 *    critical message routing services including the addressing of
 *    messages, the routing of responses, limiting message propagation,
 *    ordering of messages, and the unique identification of transactions.
 *    These header fields are in addition to the mandatory request line,
 *    which contains the method, Request-URI, and SIP version.
 *
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
