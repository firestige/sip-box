package io.netty.handler.codec.sip;

import java.net.InetSocketAddress;

/**
 * @author firestige
 * @version [version], 2021-08-16
 * @since [version]
 */
public class SipRequestDecoder extends SipObjectDecoder {

    public SipRequestDecoder() {}

    public SipRequestDecoder(int maxInitialLineLength,
                             int maxHeaderSize,
                             int maxChunkSize) {
        super(maxInitialLineLength, maxHeaderSize, maxChunkSize);
    }

    public SipRequestDecoder(int maxInitialLineLength,
                             int maxHeaderSize,
                             int maxChunkSize,
                             boolean validateHeaders) {
        super(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders);
    }

    public SipRequestDecoder(int maxInitialLineLength,
                             int maxHeaderSize,
                             int maxChunkSize,
                             boolean validateHeaders,
                             int initialBufferSize) {
        super(maxInitialLineLength,
                maxHeaderSize,
                maxChunkSize,
                validateHeaders,
                initialBufferSize);
    }

    @Override
    protected boolean isDecodingRequest() {
        return true;
    }

    @Override
    protected SipMessage createMessage(String[] initialLine, InetSocketAddress address) throws Exception {
        return new DefaultSipRequest(
                SipVersion.valueOf(initialLine[2]),
                SipMethod.valueOf(initialLine[0]),
                initialLine[1],
                validateHeaders);
    }

    @Override
    protected SipMessage createInvalidMessage(InetSocketAddress address) {
        return new DefaultSipRequest(
                SipVersion.SIP_2_0,
                SipMethod.INFO,
                "bad-request");
    }
}
