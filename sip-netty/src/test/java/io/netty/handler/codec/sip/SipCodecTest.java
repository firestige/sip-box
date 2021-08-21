package io.netty.handler.codec.sip;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.Locale;

import static io.netty.handler.codec.http.HttpConstants.CR;
import static io.netty.handler.codec.http.HttpConstants.LF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author firestige
 * @version [version], 2021-08-22
 * @since [version]
 */
public class SipCodecTest {
    private static final String CRLF = "" + (char)CR + (char)LF;

    private static final byte[] REQUEST_CONTENT_BYTES = (""
            + "<?xml version=\"1.0\" encoding=\"gb2312\"?>" + CRLF
            + "<Control>" + CRLF
            + "<CmdType>DeviceControl</CmdType>" + CRLF
            + "<SN>1</SN>" + CRLF
            + "<DeviceID>34020000002610027030</DeviceID>" + CRLF
            + "<PTZCmd>A50F0A02818100C2</PTZCmd>" + CRLF
            + "<Info>" + CRLF
            + "<ControlPriority>5</ControlPriority>" + CRLF
            + "</Info>" + CRLF
            + "</Control>").getBytes(Charset.forName("GB2312"));
    private static final byte[] REQUEST_HEADER_BYTES = (""
            +"MESSAGE sip:34020000002610027030@192.168.1.10:6626;transport=UDP SIP/2.0" + CRLF
            + "Via: SIP/2.0/UDP 172.17.0.2:57030;branch=z9hG4bK-524287-1---4119157c627d9600;rport" + CRLF
            + "Max-Forwards: 70" + CRLF
            + "To: <sip:34020000002610027030@3402000000>" + CRLF
            + "From: <sip:34020000002000000001@3402000000>;tag=36c7c40c" + CRLF
            + "Call-ID: MF5dxttskwkTEISUAkpb2Qsa" + CRLF
            + "CSeq: 2 MESSAGE" + CRLF
            + "Allow: REGISTER, INVITE, MESSAGE, ACK, BYE, CANCEL, INFO, SUBSCRIBE, NOTIFY" + CRLF
            + "Content-Type: Application/MANSCDP+xml" + CRLF
            + "User-Agent: SYSZUX28181" + CRLF
            + "Content-Length: " + REQUEST_CONTENT_BYTES.length + CRLF + CRLF).getBytes();

    private static final SipRequest MESSAGE_REQUEST = new DefaultFullSipRequest(SipVersion.SIP_2_0, SipMethod.MESSAGE
            , "sip:34020000002610027030@192.168.1.10:6626;transport=UDP");

    @Nested
    class SipRequestDecoderTest {
        @Test
        void should_success_when_decode_message_request() {
            EmbeddedChannel channel = new EmbeddedChannel(
                    new SipRequestDecoder(),
                    new SipObjectAggregator(6000));
            assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(REQUEST_HEADER_BYTES, REQUEST_CONTENT_BYTES)));
            assertTrue(channel.finish());

            SipMessage message = channel.readInbound();
            assertNotNull(message);
            assertTrue(message instanceof FullSipRequest);
            assertEquals("2 MESSAGE", message.headers().get(SipHeaderNames.CSEQ));
        }
    }

    @Nested
    class SipRequestEncoderTest {
        @Test
        void should_success_when_encode_message_request() {
            EmbeddedChannel channel = new EmbeddedChannel(
                    new SipRequestEncoder());
            ByteBuf content = Unpooled.copiedBuffer(REQUEST_CONTENT_BYTES);
            DefaultFullSipRequest request = new DefaultFullSipRequest(MESSAGE_REQUEST.protocolVersion(),
                    MESSAGE_REQUEST.method(), MESSAGE_REQUEST.uri(), content);
            request.headers()
                    .set(SipHeaderNames.FROM, "<sip:34020000002000000001@3402000000>;tag=36c7c40c")
                    .set(SipHeaderNames.TO, "<sip:34020000002610027030@3402000000>")
                    .set(SipHeaderNames.CALL_ID, "MF5dxttskwkTEISUAkpb2Qsa")
                    .setInt(SipHeaderNames.MAX_FORWARDS, 70)
                    .set(SipHeaderNames.CSEQ, "1 " + MESSAGE_REQUEST.method().name().toUpperCase(Locale.ROOT))
                    .set(SipHeaderNames.VIA, "SIP/2.0/UDP 172.17.0.2:57030;branch=z9hG4bK-524287-1---4119157c627d9600;rport")
                    .setInt(SipHeaderNames.CONTENT_LENGTH, REQUEST_CONTENT_BYTES.length)
                    .set(SipHeaderNames.CONTENT_TYPE, SipHeaderValues.APPLICATION_MANSCDP);
            assertTrue(channel.writeOutbound(request));
            assertTrue(channel.finish());

            Object buf = channel.readOutbound();
            assertNotNull(buf);
            assertTrue(buf instanceof ByteBuf);
            System.out.println(((ByteBuf) buf).toString(CharsetUtil.UTF_8));
        }
    }
}
