package io.netty.handler.codec.sip;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.CharsetUtil;

import java.util.Optional;

import static io.netty.handler.codec.sip.SipConstants.SP;

/**
 * @author firestige
 * @version [version], 2021-08-22
 * @since [version]
 */
public class SipRequestEncoder extends SipObjectEncoder<SipRequest> {
    private static final char SLASH = '/';
    private static final int SLASH_AND_SPACE_SHORT = (SLASH << 8) | SP;
    private static final int SPACE_SLASH_AND_SPACE_MEDIUM = (SP << 10) | SLASH_AND_SPACE_SHORT;

    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        return super.acceptOutboundMessage(msg) && (msg instanceof SipRequest);
    }

    @Override
    protected void encodeInitialLine(ByteBuf buf, SipRequest request) throws RuntimeException {
        encodeMethod(buf, request);
        buf.writeByte(SP);
        encodeUri(buf, request);
        buf.writeByte(SP);
        encodeProtocol(buf, request);
        ByteBufUtil.writeShortBE(buf, CRLF_SHORT);
    }

    private void encodeMethod(ByteBuf buf, SipRequest request) {
        ByteBufUtil.copy(request.method().asciiName(), buf);
    }

    private void encodeUri(ByteBuf buf, SipRequest request) {
        String requestUri = Optional.of(request)
                .map(SipRequest::uri)
                .filter(uri -> !uri.isEmpty())
                .orElse("");
        buf.writeCharSequence(requestUri, CharsetUtil.UTF_8);
    }

    private void encodeProtocol(ByteBuf buf, SipRequest request) {
        request.protocolVersion().encode(buf);
    }
}
