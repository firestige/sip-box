package io.netty.handler.codec.sip;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.http.HttpConstants.SP;
import static io.netty.util.ByteProcessor.FIND_ASCII_SPACE;
import static java.lang.Integer.parseInt;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public class SipResponseStatus implements Comparable<SipResponseStatus> {

    /**
     * {@code 100} Trying
     */
    public static final SipResponseStatus TRYING = newStatus(100, "Trying");
    /**
     * {@code 180} Ringing
     */
    public static final SipResponseStatus RINGING = newStatus(180, "Ringing");
    /**
     * {@code 181} Call Is Being Forwarded
     */
    public static final SipResponseStatus CALL_IS_BEING_FORWARDED = newStatus(181, "Call Is Being Forwarded");
    /**
     * {@code 182} Queued
     */
    public static final SipResponseStatus QUEUED = newStatus(182, "Queued");
    /**
     * {@code 183} Session Progress
     */
    public static final SipResponseStatus SESSION_PROGRESS = newStatus(183, "Session Progress");
    /**
     * {@code 200} OK
     */
    public static final SipResponseStatus OK = newStatus(200, "OK");
    /**
     * {@code 300} Multiple Choices
     */
    public static final SipResponseStatus MULTIPLE_CHOICES = newStatus(300, "Multiple Choices");
    /**
     * {@code 301} Moved Permanently
     */
    public static final SipResponseStatus MOVED_PERMANENTLY = newStatus(301, "Moved Permanently");
    /**
     * {@code 302} "Moved Temporarily
     */
    public static final SipResponseStatus MOVED_TEMPORARILY = newStatus(302, "Moved Temporarily");
    /**
     * {@code 305} Use Proxy
     */
    public static final SipResponseStatus USE_PROXY = newStatus(305, "Use Proxy");
    /**
     * {@code 380,} Alternative Service
     */
    public static final SipResponseStatus ALTERNATIVE_SERVICE = newStatus(380, "Alternative Service");
    /**
     * {@code 400} Bad Request
     */
    public static final SipResponseStatus BAD_REQUEST = newStatus(400, "Bad Request");
    /**
     * {@code 401} Unauthorized
     */
    public static final SipResponseStatus UNAUTHORIZED = newStatus(401, "Unauthorized");
    /**
     * {@code 402} Payment Required
     */
    public static final SipResponseStatus PAYMENT_REQUIRED = newStatus(402, "Payment Required");
    /**
     * {@code 403} Forbidden
     */
    public static final SipResponseStatus FORBIDDEN = newStatus(403, "Forbidden");
    /**
     * {@code 404 NOT FOUND
     */
    public static final SipResponseStatus NOT_FOUND = newStatus(404, "NOT FOUND");
    /**
     * {@code 405} Method Not Allowed
     */
    public static final SipResponseStatus METHOD_NOT_ALLOWED = newStatus(405, "Method Not Allowed");
    /**
     * {@code 406} Not Acceptable
     */
    public static final SipResponseStatus NOT_ACCEPTABLE = newStatus(406, "Not Acceptable");
    /**
     * {@code 407} Proxy Authentication Required
     */
    public static final SipResponseStatus PROXY_AUTHENTICATION_REQUIRED = newStatus(407, "Proxy Authentication " +
            "Required");
    /**
     * {@code 408} Request Timeout
     */
    public static final SipResponseStatus REQUEST_TIMEOUT = newStatus(408, "Request Timeout");
    /**
     * {@code 410} GONE
     */
    public static final SipResponseStatus GONE = newStatus(410, "GONE");
    /**
     * {@code 413} Request Entity Too Large
     */
    public static final SipResponseStatus REQUEST_ENTITY_TOO_LARGE = newStatus(413, "Request Entity Too Large");
    /**
     * {@code 414} Request-URI Too Long
     */
    public static final SipResponseStatus REQUEST_URI_TOO_LONG = newStatus(414, "Request-URI Too Long");
    /**
     * {@code 415} Unsupported Media Type
     */
    public static final SipResponseStatus UNSUPPORTED_MEDIA_TYPE = newStatus(415, "Unsupported Media Type");
    /**
     * {@code 416} Unsupported URI Scheme
     */
    public static final SipResponseStatus UNSUPPORTED_URI_SCHEME = newStatus(416, "Unsupported URI Scheme");
    /**
     * {@code 420} Bad Extension
     */
    public static final SipResponseStatus BAD_EXTENSION = newStatus(420, "Bad Extension");
    /**
     * {@code 421} Extension Required
     */
    public static final SipResponseStatus EXTENSION_REQUIRED = newStatus(421, "Extension Required");
    /**
     * {@code 423} Interval Too Brief
     */
    public static final SipResponseStatus INTERVAL_TOO_BRIEF = newStatus(423, "Interval Too Brief");
    /**
     * {@code 480} Temporarily Unavailable
     */
    public static final SipResponseStatus TEMPORARILY_UNAVAILABLE = newStatus(480, "Temporarily Unavailable");
    /**
     * {@code 481} Call/Transaction Does Not Exist
     */
    public static final SipResponseStatus CALL_OR_TRANSACTION_DOES_NOT_EXIST = newStatus(481, "Call/Transaction Does " +
            "Not Exist");
    /**
     * {@code 482} Loop Detected
     */
    public static final SipResponseStatus LOOP_DETECTED = newStatus(482, "Loop Detected");
    /**
     * {@code 483} Too Many Hops
     */
    public static final SipResponseStatus TOO_MANY_HOPS = newStatus(483, "Too Many Hops");
    /**
     * {@code 484} Address Incomplete
     */
    public static final SipResponseStatus ADDRESS_INCOMPLETE = newStatus(484, "Address Incomplete");
    /**
     * {@code 485} Ambiguous
     */
    public static final SipResponseStatus AMBIGUOUS = newStatus(485, "Ambiguous");
    /**
     * {@code 486} Busy Here
     */
    public static final SipResponseStatus BUSY_HERE = newStatus(486, "Busy Here");
    /**
     * {@code 487} Request Terminated
     */
    public static final SipResponseStatus REQUEST_TERMINATED = newStatus(487, "Request Terminated");
    /**
     * {@code 488} Not Acceptable Here
     */
    public static final SipResponseStatus NOT_ACCEPTABLE_HERE = newStatus(488, "Not Acceptable Here");
    /**
     * {@code 491} Request Pending
     */
    public static final SipResponseStatus REQUEST_PENDING = newStatus(491, "Request Pending");
    /**
     * {@code 493} Undecipherable
     */
    public static final SipResponseStatus UNDECIPHERABLE = newStatus(493, "Undecipherable");
    /**
     * {@code 500} Server Internal Error
     */
    public static final SipResponseStatus SERVER_INTERNAL_ERROR = newStatus(500, "Server Internal Error");
    /**
     * {@code 501} Not Implemented
     */
    public static final SipResponseStatus NOT_IMPLEMENTED = newStatus(501, "Not Implemented");
    /**
     * {@code 502} Bad Gateway
     */
    public static final SipResponseStatus BAD_GATEWAY = newStatus(502, "Bad Gateway");
    /**
     * {@code 503} Service Unavailable
     */
    public static final SipResponseStatus SERVICE_UNAVAILABLE = newStatus(503, "Service Unavailable");
    /**
     * {@code 504} Server Time-out
     */
    public static final SipResponseStatus SERVER_TIMEOUT = newStatus(504, "Server Time-out");
    /**
     * {@code 505} Version Not Supported
     */
    public static final SipResponseStatus VERSION_NOT_SUPPORT = newStatus(505, "Version Not Supported");
    /**
     * {@code 513} Message Too Large
     */
    public static final SipResponseStatus MESSAGE_TOO_LARGE = newStatus(513, "Message Too Large");
    /**
     * {@code 600} Busy Everywhere
     */
    public static final SipResponseStatus BUSY_EVERYWHERE = newStatus(600, "Busy Everywhere");
    /**
     * {@code 603} Decline
     */
    public static final SipResponseStatus DECLINE = newStatus(603, "Decline");
    /**
     * {@code 604} Does Not Exist Anywhere
     */
    public static final SipResponseStatus DOES_NOT_EXIST_ANYWHERE = newStatus(604, "Does Not Exist Anywhere");
    /**
     * {@code 606} Not Acceptable
     */
    public static final SipResponseStatus NOT_ACCEPTABLE_G = newStatus(606, "Not Acceptable");

    private static final Map<Integer, SipResponseStatus> STATUS_MAP;

    static {
        Map<Integer, SipResponseStatus> map = new ConcurrentHashMap<>();
        map.put(TRYING.code(), TRYING);
        map.put(RINGING.code(), RINGING);
        map.put(CALL_IS_BEING_FORWARDED.code(), CALL_IS_BEING_FORWARDED);
        map.put(QUEUED.code(), QUEUED);
        map.put(SESSION_PROGRESS.code(), SESSION_PROGRESS);
        map.put(OK.code(), OK);
        map.put(MULTIPLE_CHOICES.code(), MULTIPLE_CHOICES);
        map.put(MOVED_PERMANENTLY.code(), MOVED_PERMANENTLY);
        map.put(MOVED_TEMPORARILY.code(), MOVED_TEMPORARILY);
        map.put(USE_PROXY.code(), USE_PROXY);
        map.put(ALTERNATIVE_SERVICE.code(), ALTERNATIVE_SERVICE);
        map.put(BAD_REQUEST.code(), BAD_REQUEST);
        map.put(UNAUTHORIZED.code(), UNAUTHORIZED);
        map.put(PAYMENT_REQUIRED.code(), PAYMENT_REQUIRED);
        map.put(FORBIDDEN.code(), FORBIDDEN);
        map.put(NOT_FOUND.code(), NOT_FOUND);
        map.put(METHOD_NOT_ALLOWED.code(), METHOD_NOT_ALLOWED);
        map.put(NOT_ACCEPTABLE.code(), NOT_ACCEPTABLE);
        map.put(PROXY_AUTHENTICATION_REQUIRED.code(), PROXY_AUTHENTICATION_REQUIRED);
        map.put(REQUEST_TIMEOUT.code(), REQUEST_TIMEOUT);
        map.put(GONE.code(), GONE);
        map.put(REQUEST_ENTITY_TOO_LARGE.code(), REQUEST_ENTITY_TOO_LARGE);
        map.put(REQUEST_URI_TOO_LONG.code(), REQUEST_URI_TOO_LONG);
        map.put(UNSUPPORTED_MEDIA_TYPE.code(), UNSUPPORTED_MEDIA_TYPE);
        map.put(UNSUPPORTED_URI_SCHEME.code(), UNSUPPORTED_URI_SCHEME);
        map.put(BAD_EXTENSION.code(), BAD_EXTENSION);
        map.put(EXTENSION_REQUIRED.code(), EXTENSION_REQUIRED);
        map.put(INTERVAL_TOO_BRIEF.code(), INTERVAL_TOO_BRIEF);
        map.put(TEMPORARILY_UNAVAILABLE.code(), TEMPORARILY_UNAVAILABLE);
        map.put(CALL_OR_TRANSACTION_DOES_NOT_EXIST.code(), CALL_OR_TRANSACTION_DOES_NOT_EXIST);
        map.put(LOOP_DETECTED.code(), LOOP_DETECTED);
        map.put(TOO_MANY_HOPS.code(), TOO_MANY_HOPS);
        map.put(ADDRESS_INCOMPLETE.code(), ADDRESS_INCOMPLETE);
        map.put(AMBIGUOUS.code(), AMBIGUOUS);
        map.put(BUSY_HERE.code(), BUSY_HERE);
        map.put(REQUEST_TERMINATED.code(), REQUEST_TERMINATED);
        map.put(NOT_ACCEPTABLE_HERE.code(), NOT_ACCEPTABLE_HERE);
        map.put(REQUEST_PENDING.code(), REQUEST_PENDING);
        map.put(UNDECIPHERABLE.code(), UNDECIPHERABLE);
        map.put(SERVER_INTERNAL_ERROR.code(), SERVER_INTERNAL_ERROR);
        map.put(NOT_IMPLEMENTED.code(), NOT_IMPLEMENTED);
        map.put(BAD_GATEWAY.code(), BAD_GATEWAY);
        map.put(SERVICE_UNAVAILABLE.code(), SERVICE_UNAVAILABLE);
        map.put(SERVER_TIMEOUT.code(), SERVER_TIMEOUT);
        map.put(VERSION_NOT_SUPPORT.code(), VERSION_NOT_SUPPORT);
        map.put(MESSAGE_TOO_LARGE.code(), MESSAGE_TOO_LARGE);
        map.put(BUSY_EVERYWHERE.code(), BUSY_EVERYWHERE);
        map.put(DECLINE.code(), DECLINE);
        map.put(DOES_NOT_EXIST_ANYWHERE.code(), DOES_NOT_EXIST_ANYWHERE);
        map.put(NOT_ACCEPTABLE_G.code(), NOT_ACCEPTABLE_G);

        STATUS_MAP = Collections.unmodifiableMap(map);
    }

    private static SipResponseStatus newStatus(int statusCode, String reasonPhrase) {
        return new SipResponseStatus(statusCode, reasonPhrase, true);
    }

    public static SipResponseStatus valueOf(int code) {
        return valueOf0(code).orElseGet(() -> new SipResponseStatus(code));
    }

    private static Optional<SipResponseStatus> valueOf0(int code) {
        return Optional.ofNullable(STATUS_MAP.get(code));
    }

    public static SipResponseStatus valueOf(int code, String reasonPhrase) {
        return valueOf0(code).filter(status -> status.reasonPhrase().contains(reasonPhrase))
                .orElseGet(() -> new SipResponseStatus(code, reasonPhrase));
    }

    public static SipResponseStatus parseLine(CharSequence line) {
        return (line instanceof AsciiString) ? parseLine((AsciiString) line) : parseLine(line.toString());
    }

    public static SipResponseStatus parseLine(String line) {
        try {
            int space = line.indexOf(' ');
            return space == -1 ? valueOf(parseInt(line)) :
                    valueOf(parseInt(line.substring(0, space)), line.substring(space + 1));
        } catch (Exception e) {
            throw new IllegalArgumentException("malformed status line: " + line, e);
        }
    }

    public static SipResponseStatus parseLine(AsciiString line) {
        try {
            int space = line.forEachByte(FIND_ASCII_SPACE);
            return space == -1 ? valueOf(line.parseInt()) : valueOf(line.parseInt(0, space), line.toString(space + 1));
        } catch (Exception e) {
            throw new IllegalArgumentException("malformed status line: " + line, e);
        }
    }

    private final int code;
    private final AsciiString codeAsText;
    private SipStatusClass codeClass;

    private final String reasonPhrase;
    private final byte[] bytes;

    private SipResponseStatus(int code) {
        this(code, SipStatusClass.valueOf(code).defaultReasonPhrase() + " (" + code + ')', false);
    }

    public SipResponseStatus(int code, String reasonPhrase) {
        this(code, reasonPhrase, false);
    }

    private SipResponseStatus(int code, String reasonPhrase, boolean bytes) {
        ObjectUtil.checkPositive(code, "code");
        ObjectUtil.checkNotNull(reasonPhrase, "reasonPhrase");

        for (int i = 0; i < reasonPhrase.length(); i ++) {
            char c = reasonPhrase.charAt(i);
            // Check prohibited characters.
            if (c == '\n' || c == '\r') {
                throw new IllegalArgumentException(
                        "reasonPhrase contains one of the following prohibited characters: " +
                                "\\r\\n: " + reasonPhrase);
            }
        }

        this.code = code;
        String codeString = Integer.toString(code);
        codeAsText = new AsciiString(codeString);
        this.reasonPhrase = reasonPhrase;
        this.bytes = bytes ? (codeString + ' ' + reasonPhrase).getBytes(CharsetUtil.US_ASCII) : null;
    }

    public int code() {
        return code;
    }

    public AsciiString codeAsText() {
        return codeAsText;
    }

    public SipStatusClass codeClass() {
        return Optional.ofNullable(this.codeClass).orElseGet(() -> {
            this.codeClass = SipStatusClass.valueOf(code);
            return this.codeClass;
        });
    }

    public String reasonPhrase() {
        return reasonPhrase;
    }

    @Override
    public int hashCode() {
        return code();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SipResponseStatus && code() == ((SipResponseStatus) obj).code();
    }

    @Override
    public String toString() {
        return new StringBuilder(reasonPhrase.length() + 4)
                .append(codeAsText)
                .append(' ')
                .append(reasonPhrase)
                .toString();
    }

    @Override
    public int compareTo(SipResponseStatus o) {
        return code() - o.code();
    }

    void encode(ByteBuf buf) {
        if (bytes == null) {
            ByteBufUtil.copy(codeAsText, buf);
            buf.writeByte(SP);
            buf.writeCharSequence(reasonPhrase, CharsetUtil.US_ASCII);
        } else {
            buf.writeBytes(bytes);
        }
    }
}
