package io.netty.handler.codec.sip;

import io.netty.handler.codec.DecoderResult;

/**
 * @author firestige
 * @version [version], 2021-08-16
 * @since [version]
 */
public class SipMessageDecoderResult extends DecoderResult {

    private final int initialLineLength;
    private final int headerSize;

    SipMessageDecoderResult(int initialLineLength, int headerSize) {
        super(SIGNAL_SUCCESS);
        this.initialLineLength = initialLineLength;
        this.headerSize = headerSize;
    }

    public int initialLineLength() {
        return initialLineLength;
    }

    public int headerSize() {
        return headerSize;
    }

    public int totalSize() {
        return initialLineLength + headerSize;
    }
}
