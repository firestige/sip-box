package io.firestigeiris.sipbox;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.sip.DefaultSipRequest;
import io.netty.handler.codec.sip.DefaultSipResponse;
import io.netty.handler.codec.sip.SipConstants;
import io.netty.handler.codec.sip.SipMessage;
import io.netty.handler.codec.sip.SipMethod;
import io.netty.handler.codec.sip.SipObjectDecoder;
import io.netty.handler.codec.sip.SipResponseStatus;
import io.netty.handler.codec.sip.SipVersion;
import io.netty.util.ByteProcessor;
import io.netty.util.internal.AppendableCharSequence;

import java.util.List;

/**
 * @author firestige
 * @version [version], 2021-08-21
 * @since [version]
 */
public class SipDecoder extends ByteToMessageDecoder {

    private AppendableCharSequence seq = new AppendableCharSequence(256);
    private final LineParser lineParser = new LineParser(seq, 128);
    private HeaderParser headerParser = new HeaderParser(seq, 1024);

    private SipMessage message;

    /**
     The internal state of {@link SipDecoder}.
     * <em>Internal use only</em>.
     */
    private enum State {
        SKIP_CONTROL_CHARS,
        READ_INITIAL,
        READ_HEADER,
        READ_FIXED_LENGTH_CONTENT,
        BAD_MESSAGE,
    }

    private State currentState = State.SKIP_CONTROL_CHARS;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        State next = readInitial(in, out);
        if (next == null) {
            return;
        }
        currentState = next;
        if (currentState == State.READ_HEADER) {
            System.out.println("read header");
            if (message != null) {
                out.add(message);
            }
        }
        in.skipBytes(in.readableBytes());
//        if (currentState == State.BAD_MESSAGE) {
//            in.skipBytes(in.readableBytes());
//        }
    }

    private State readInitial(ByteBuf buf, List<Object> out) {
        try {
            AppendableCharSequence line = lineParser.parse(buf);
            if (line == null) {
                return null;
            }
            String[] initialLine = splitInitialLine(line);
            if (initialLine.length < 3) {
                // Invalid initil line -> ignore
                currentState = State.SKIP_CONTROL_CHARS;
                return State.SKIP_CONTROL_CHARS;
            }
            message = createMessage(initialLine);
            return State.READ_HEADER;
        } catch (Exception e) {
            out.add(invalidMessage(buf, e));
        }
        return null;
    }

    private SipMessage createMessage(String[] initialLine) {
        return SipVersion.SIP_2_0.text().equals(initialLine[0])
                ? new DefaultSipResponse(
                    SipVersion.valueOf(initialLine[0]),
                    SipResponseStatus.parseLine(initialLine[1]+ " " + initialLine[2]),
                    true)
                : new DefaultSipRequest(
                    SipVersion.valueOf(initialLine[2]),
                    SipMethod.valueOf(initialLine[0]),
                    initialLine[1],
                    true);
    }

    private SipMessage invalidMessage(ByteBuf buf, Exception cause) {
        currentState = State.BAD_MESSAGE;
        buf.skipBytes(buf.readableBytes());
        if (message == null) {
            message = createInvalidMessage();
        }
        message.setDecoderResult(DecoderResult.failure(cause));
        SipMessage ret = message;
        message = null;
        return ret;
    }

    private SipMessage createInvalidMessage() {
        return new DefaultSipResponse(SipVersion.SIP_2_0, new SipResponseStatus(999, "Unknown"), true);
    }

    private static String[] splitInitialLine(AppendableCharSequence line) {
        int aStart;
        int aEnd;
        int bStart;
        int bEnd;
        int cStart;
        int cEnd;

        aStart = findNonSPLenient(line, 0);
        aEnd = findSPLenient(line, aStart);

        bStart = findNonSPLenient(line, aEnd);
        bEnd = findSPLenient(line, bStart);

        cStart = findNonSPLenient(line, bEnd);
        cEnd = findEndOfString(line);

        return new String[] {
                line.subStringUnsafe(aStart, aEnd),
                line.subStringUnsafe(bStart, bEnd),
                cStart < cEnd? line.subStringUnsafe(cStart, cEnd) : "" };
    }

    private static int findNonSPLenient(AppendableCharSequence seq, int offset) {
        for (int result = offset; result < seq.length(); ++result) {
            char c = seq.charAt(result);
            if (isSPLenient(c)) {
                continue;
            }
            if (Character.isWhitespace(c)) {
                throw new IllegalArgumentException("Invalid separator");
            }
            return result;
        }
        return seq.length();
    }

    private static int findSPLenient(AppendableCharSequence seq, int offset) {
        for (int result = offset; result < seq.length(); ++result) {
            if (isSPLenient(seq.charAtUnsafe(result))) {
                return result;
            }
        }
        return seq.length();
    }

    private static boolean isSPLenient(char c) {
        return c == ' '
                || c == (char) 0x09
                || c == (char) 0x0B
                || c == (char) 0x0C
                || c == (char) 0x0D;
    }

    private static int findNonWhitespace(AppendableCharSequence seq, int offset, boolean validateOWS) {
        for (int result = offset; result < seq.length(); ++result) {
            char c = seq.charAtUnsafe(result);
            if (!Character.isWhitespace(c)) {
                return result;
            } else if (validateOWS && !isOWS(c)) {
                // Only OWS is supported for whitespace
                throw new IllegalArgumentException("Invalid separator, only a single space or horizontal tab allowed," +
                        " but received a '" + c + "' (0x" + Integer.toHexString(c) + ")");
            }
        }
        return seq.length();
    }

    private static int findEndOfString(AppendableCharSequence seq) {
        for (int result = seq.length() - 1; result > 0; --result) {
            if (!Character.isWhitespace(seq.charAtUnsafe(result))) {
                return result + 1;
            }
        }
        return 0;
    }

    private static boolean isOWS(char ch) {
        return ch == ' ' || ch == (char) 0x09;
    }


    /**
     * 头域解析器
     */
    private static class HeaderParser implements ByteProcessor {
        private final AppendableCharSequence seq;
        private final int maxLength;
        int size;

        HeaderParser(AppendableCharSequence seq, int maxLength) {
            this.seq = seq;
            this.maxLength = maxLength;
        }
        public AppendableCharSequence parse(ByteBuf buffer) {
            final int oldSize = size;
            seq.reset();
            int i = buffer.forEachByte(this);
            if (i == -1) {
                size = oldSize;
                return null;
            }
            buffer.readerIndex(i + 1);
            return seq;
        }

        public void reset() {
            size = 0;
        }

        @Override
        public boolean process(byte value) throws Exception {
            char nextByte = (char) (value & 0xFF);
            if (nextByte == SipConstants.LF) {
                int len = seq.length();
                // Drop CR if we had a CRLF pair
                if (len >= 1 && seq.charAtUnsafe(len - 1) == SipConstants.CR) {
                    -- size;
                    seq.setLength(len - 1);
                }
                return false;
            }

            increaseCount();

            seq.append(nextByte);
            return true;
        }

        protected final void increaseCount() {
            if (++ size > maxLength) {
                // TODO: Respond with Bad Request and discard the traffic
                //    or close the connection.
                //       No need to notify the upstream handlers - just log.
                //       If decoding a response, just throw an exception.
                throw newException(maxLength);
            }
        }

        protected TooLongFrameException newException(int maxLength) {
            return new TooLongFrameException("Sip header is larger than " + maxLength + " bytes.");
        }
    }

    /**
     * 消息行解析器
     */
    private final class LineParser extends HeaderParser {
        LineParser(AppendableCharSequence seq, int maxLength) {
            super(seq, maxLength);
        }

        @Override
        public AppendableCharSequence parse(ByteBuf buffer) {
            reset();
            return super.parse(buffer);
        }

        @Override
        public boolean process(byte value) throws Exception {
            if (currentState == State.SKIP_CONTROL_CHARS) {
                char c = (char) (value & 0xFF);
                if (Character.isISOControl(c) || Character.isWhitespace(c)) {
                    increaseCount();
                    return true;
                }
                currentState = State.READ_INITIAL;
            }
            return super.process(value);
        }

        @Override
        protected TooLongFrameException newException(int maxLength) {
            return new TooLongFrameException("An Sip line is larger than " + maxLength + " bytes.");
        }
    }
}
