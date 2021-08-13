package io.netty.handler.codec.sip;

import io.netty.util.AsciiString;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public enum SipStatusClass {

    /**
     * The informational class (1xx)
     */
    INFORMATIONAL(100, 200, "Informational"),
    /**
     * The success class (2xx)
     */
    SUCCESS(200, 300, "Success"),
    /**
     * The redirection class (3xx)
     */
    REDIRECTION(300, 400, "Redirection"),
    /**
     * The client error class (4xx)
     */
    CLIENT_ERROR(400, 500, "Client Error"),
    /**
     * The server error class (5xx)
     */
    SERVER_ERROR(500, 600, "Server Error"),
    /**
     * The Global error class (6xx)
     */
    GLOBAL_ERROR(600, 700, "Global Error"),
    /**
     * The unknown class
     */
    UNKNOWN(0, 0, "Unknown Status") {
        @Override
        public boolean contains(int code) {
            return code < 100 || code >= 700;
        }
    };

    public static SipStatusClass valueOf(int code) {
        if (INFORMATIONAL.contains(code)) {
            return INFORMATIONAL;
        }
        if (SUCCESS.contains(code)) {
            return SUCCESS;
        }
        if (REDIRECTION.contains(code)) {
            return REDIRECTION;
        }
        if (CLIENT_ERROR.contains(code)) {
            return CLIENT_ERROR;
        }
        if (SERVER_ERROR.contains(code)) {
            return SERVER_ERROR;
        }
        if (GLOBAL_ERROR.contains(code)) {
            return GLOBAL_ERROR;
        }
        return UNKNOWN;
    }

    public static SipStatusClass valueOf(CharSequence code) {
        if (code != null && code.length() == 3) {
            char c0 = code.charAt(0);
            return isDigit(c0) && isDigit(code.charAt(1)) && isDigit(code.charAt(2)) ? valueOf(digit(c0) * 100) :
                    UNKNOWN;
        }
        return UNKNOWN;
    }

    private static int digit(char c) {
        return c - '0';
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private final int min;
    private final int max;
    private final AsciiString defaultReasonPhrase;

    SipStatusClass(int min, int max, String defaultReasonPhrase) {
        this.min = min;
        this.max = max;
        this.defaultReasonPhrase = AsciiString.cached(defaultReasonPhrase);
    }

    /**
     * Returns {@code true} if and only if the specified Sip status code falls into this class.
     */
    public boolean contains(int code) {
        return code >= min && code < max;
    }

    /**
     * Returns the default reason phrase of this Sip status class.
     */
    AsciiString defaultReasonPhrase() {
        return defaultReasonPhrase;
    }

}
