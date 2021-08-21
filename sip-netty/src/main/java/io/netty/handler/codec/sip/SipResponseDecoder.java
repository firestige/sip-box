package io.netty.handler.codec.sip;

import java.net.InetSocketAddress;

/**
 * @author firestige
 * @version [version], 2021-08-17
 * @since [version]
 */
public class SipResponseDecoder extends SipObjectDecoder {

    private static final SipResponseStatus UNKNOWN_STATUS = new SipResponseStatus(999, "Unknown");

    public SipResponseDecoder() {}

    public SipResponseDecoder(int maxInitialLineLength,
                              int maxHeaderSize,
                              int maxChunkSize) {
        super(maxInitialLineLength, maxHeaderSize, maxChunkSize);
    }

    public SipResponseDecoder(int maxInitialLineLength,
                              int maxHeaderSize,
                              int maxChunkSize,
                              boolean validateHeaders) {
        super(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders);
    }

    public SipResponseDecoder(int maxInitialLineLength,
                              int maxHeaderSize,
                              int maxChunkSize,
                              boolean validateHeaders,
                              int initialBufferSize) {
        super(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders, initialBufferSize);
    }

    @Override
    protected boolean isDecodingRequest() {
        return false;
    }

    @Override
    protected SipMessage createMessage(String[] initialLine) throws Exception {
        return new DefaultSipResponse(
                SipVersion.valueOf(initialLine[0]),
                SipResponseStatus.parseLine(initialLine[1]+ " " + initialLine[2]),
                validateHeaders);
    }

    @Override
    protected SipMessage createInvalidMessage() {
        return new DefaultSipResponse(SipVersion.SIP_2_0, UNKNOWN_STATUS, validateHeaders);
    }
}
