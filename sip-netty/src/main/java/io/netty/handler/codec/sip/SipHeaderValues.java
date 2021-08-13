package io.netty.handler.codec.sip;

import io.netty.util.AsciiString;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public final class SipHeaderValues {
    /**
     * {@code "Application/MANSCDP+xml"}
     */
    public static final AsciiString APPLICATION_MANSCDP = AsciiString.cached("Application/MANSCDP+xml");
    /**
     * {@code "application/json"}
     */
    public static final AsciiString APPLICATION_JSON = AsciiString.cached("application/json");
    /**
     * {@code "application/octet-stream"}
     */
    public static final AsciiString APPLICATION_OCTET_STREAM = AsciiString.cached("application/octet-stream");
    /**
     * {@code "Application/Sdp"}
     */
    public static final AsciiString APPLICATION_SDP = AsciiString.cached("Application/Sdp");
    /**
     * {@code "application/xhtml+xml"}
     */
    public static final AsciiString APPLICATION_XHTML = AsciiString.cached("application/xhtml+xml");
    /**
     * {@code "application/xml"}
     */
    public static final AsciiString APPLICATION_XML = AsciiString.cached("application/xml");
    /**
     * {@code "charset"}
     */
    public static final AsciiString CHARSET = AsciiString.cached("charset");
    /**
     * {@code "multipart/form-data"}
     */
    public static final AsciiString MULTIPART_FORM_DATA = AsciiString.cached("multipart/form-data");
    /**
     * {@code "multipart/mixed"}
     */
    public static final AsciiString MULTIPART_MIXED = AsciiString.cached("multipart/mixed");
    /**
     * {@code "none"}
     */
    public static final AsciiString NONE = AsciiString.cached("none");
    /**
     * {@code "0"}
     */
    public static final AsciiString ZERO = AsciiString.cached("0");
    /**
     * {@code "text/css"}
     */
    public static final AsciiString TEXT_CSS = AsciiString.cached("text/css");
    /**
     * {@code "text/html"}
     */
    public static final AsciiString TEXT_HTML = AsciiString.cached("text/html");
    /**
     * {@code "text/event-stream"}
     */
    public static final AsciiString TEXT_EVENT_STREAM = AsciiString.cached("text/event-stream");
    /**
     * {@code "text/plain"}
     */
    public static final AsciiString TEXT_PLAIN = AsciiString.cached("text/plain");

    private SipHeaderValues() {}
}
