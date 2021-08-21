package io.netty.handler.codec.sip;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.ByteProcessor;
import io.netty.util.internal.AppendableCharSequence;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;

import static io.netty.util.internal.ObjectUtil.checkPositive;

/**
 * Decodes {@link ByteBuf}s into {@link SipMessage}s and {@link SipContent}s
 * There is no boundary defined in RFC,
 * Below are few restrictions for {@link SipMessage} and {@link SipHeaders}, and note that is not official
 *
 * <h2>The Restrictions</h2>
 *
 * <h4>1. SIP Restrictions</h4>
 * <hr />
 * <table border="1">
 *     <tr>
 *         <th>Name</th>
 *         <th>Limit</th>
 *         <th>Restrictive Action</th>
 *     </tr>
 *     <tr>
 *         <td>Whole Message</td>
 *         <td>6048 Bytes</td>
 *         <td>Discard the signal</td>
 *     </tr>
 *     <tr>
 *         <td>Whole Message Body(Excluding CRLF)</td>
 *         <td>3000 Bytes</td>
 *         <td>413 Request Entity Too Large</td>
 *     </tr>
 *     <tr>
 *         <td>Each Message Body Line</td>
 *         <td>No Restriction</td>
 *         <td>No Restriction</td>
 *     </tr>
 *     <tr>
 *         <td>Request Line\Status Line</td>
 *         <td>256 Bytes</td>
 *         <td>400 Bad Request</td>
 *     </tr>
 *     <tr>
 *         <td>URI</td>
 *         <td>1024 Bytes</td>
 *         <td>414 Request URI Too Long</td>
 *     </tr>
 * </table>
 * <br />
 * <h4>2. SIP Header Restrictions</h4>
 * <hr />
 * <table border="1">
 *     <tr>
 *         <th>Headers</th>
 *         <th>Limit(Max Bytes, Max Headers) Includes CRLF</th>
 *         <th>Restrictive Action</th>
 *     </tr>
 *     <tr>
 *         <td>Call-ID</td>
 *         <td>136 Bytes, 1 Max</td>
 *         <td>400 Bad Request</td>
 *     </tr>
 *     <tr>
 *         <td>Privacy</td>
 *         <td>640 Bytes, 6 Max</td>
 *         <td>400 Bad Request</td>
 *     </tr>
 *     <tr>
 *         <td>Reason</td>
 *         <td>640 Bytes, No Limit</td>
 *         <td>400 Bad Request</td>
 *     </tr>
 *     <tr>
 *         <td>Record-Route</td>
 *         <td>512 Bytes, 6 Max</td>
 *         <td>400 Bad Request</td>
 *     </tr>
 *     <tr>
 *         <td>Route</td>
 *         <td>512 Bytes, 6 Max</td>
 *         <td>400 Bad Request</td>
 *     </tr>
 *     <tr>
 *         <td>Via</td>
 *         <td>512 Bytes, 6 Max</td>
 *         <td>400 Bad Request</td>
 *     </tr>
 *     <tr>
 *         <td>xxxxx (All other Headers)</td>
 *         <td>256 Bytes, No Limit</td>
 *         <td>400 Bad Request</td>
 *     </tr>
 * </table>
 * <br />
 * <h4>3. Parameters</h4>
 * <hr />
 * <table border="1">
 *     <tr>
 *         <th>Name</th><th>Default value</th><th>Meaning</th>
 *     </tr>
 *     <tr>
 *         <td>{@code maxInitialLineLength}</td>
 *         <td>{@value #DEFAULT_MAX_INITIAL_LINE_LENGTH}</td>
 *         <td>The maximum length of the initial line
 *              (e.g. {@code "INVITE sip:bob@192.0.2.4 SIP/2.0"} or {@code "SIP/2.0 200 OK"})
 *              If the length of the initial line exceeds this value, a
 *              {@link TooLongFrameException} will be raised.</td>
 *     </tr>
 *     <tr>
 *          <td>{@code maxHeaderSize}</td>
 *          <td>{@value #DEFAULT_MAX_HEADER_SIZE}</td>
 *          <td>The maximum length of all headers.  If the sum of the length of each
 *              header exceeds this value, a {@link TooLongFrameException} will be raised.</td>
 *     </tr>
 *     <tr>
 *          <td>{@code maxChunkSize}</td>
 *          <td>{@value #DEFAULT_MAX_CHUNK_SIZE}</td>
 *          <td>The maximum length of the whole message. If the size of whole SipObject
 *              exceeds this value, a {@link TooLongFrameException} will be raised.</td>
 *     </tr>
 * </table>
 * <br />
 * @author firestige
 * @version [version], 2021-08-16
 * @since [version]
 */
public abstract class SipObjectDecoder extends ByteToMessageDecoder {
    // pre-setting
    public static final int DEFAULT_MAX_INITIAL_LINE_LENGTH = 256;
    public static final int DEFAULT_MAX_HEADER_SIZE = 3072;
    public static final boolean DEFAULT_VALIDATE_HEADERS = true;
    public static final int DEFAULT_INITIAL_BUFFER_SIZE = 128;
    public static final int DEFAULT_MAX_CHUNK_SIZE = 6048;

    private static final String EMPTY_VALUE = "";

    private final int maxChunkSize;
    protected final boolean validateHeaders;
    private final HeaderParser headerParser;
    private final LineParser lineParser;

    private SipMessage message;
    /**
     * mark for current chunk size
     */
    private int chunkSize;
    /**
     * mark for content-length, if is negative, indicate not found content-length
     */
    private int contentLength = Integer.MIN_VALUE;
    /**
     * flag, if true means need call {@link #resetNow()}
     */
    private volatile boolean resetRequested;

    /**
     * headername, will be updated by {@link #splitHeader(AppendableCharSequence)}
     */
    private CharSequence name;
    /**
     * header value, will be updated by {@link #splitHeader(AppendableCharSequence)}
     */
    private CharSequence value;

    /**
     The internal state of {@link SipObjectDecoder}.
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

    protected SipObjectDecoder() {
        this(DEFAULT_MAX_INITIAL_LINE_LENGTH,
                DEFAULT_MAX_HEADER_SIZE,
                DEFAULT_MAX_CHUNK_SIZE,
                DEFAULT_VALIDATE_HEADERS,
                DEFAULT_INITIAL_BUFFER_SIZE);
    }

    protected SipObjectDecoder(int maxInitialLineLength,
                               int maxHeaderSize,
                               int maxChunkSize){
        this(maxInitialLineLength,
                maxHeaderSize,
                maxChunkSize,
                DEFAULT_VALIDATE_HEADERS);
    }

    protected SipObjectDecoder(int maxInitialLineLength,
                               int maxHeaderSize,
                               int maxChunkSize,
                               boolean validateHeaders) {
        this(maxInitialLineLength,
                maxHeaderSize,
                maxChunkSize,
                validateHeaders,
                DEFAULT_INITIAL_BUFFER_SIZE);
    }

    protected SipObjectDecoder(int maxInitialLineLength,
                            int maxHeaderSize,
                            int maxChunkSize,
                            boolean validateHeaders,
                            int initialBufferSize) {
        checkPositive(maxInitialLineLength, "maxInitialLineLength");
        checkPositive(maxHeaderSize, "maxHeaderSize");
        checkPositive(maxChunkSize, "maxChunkSize");

        AppendableCharSequence seq = new AppendableCharSequence(initialBufferSize);
        lineParser = new LineParser(seq, maxInitialLineLength);
        headerParser = new HeaderParser(seq, maxHeaderSize);
        this.maxChunkSize = maxChunkSize;
        this.validateHeaders = validateHeaders;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
       if (resetRequested) {
            resetNow();
        }
        boolean next;
        if (currentState == State.READ_INITIAL || currentState == State.SKIP_CONTROL_CHARS) {
            next = readInitial(buf, out);
            if (!next) {
                return;
            }
        }
        if (currentState == State.READ_HEADER) {
            next = readHeader(buf, out);
            if (!next) {
                return;
            }
        }
        if (currentState == State.READ_FIXED_LENGTH_CONTENT) {
            readContent(buf, out);
            return;
        }
        if (currentState == State.BAD_MESSAGE) {
            buf.skipBytes(buf.readableBytes());
        }
    }

    private boolean readInitial(ByteBuf buf, List<Object> out) {
        boolean ret = false;
        try {
            // 解析首行
            AppendableCharSequence line = lineParser.parse(buf);
            if (line == null) {
                return ret;
            }
            String[] initialLine = splitInitialLine(line);
            if (initialLine.length < 3) {
                // Invalid initil line -> ignore
                currentState = State.SKIP_CONTROL_CHARS;
                return ret;
            }
            // 创建对象
            message = createMessage(initialLine);
            currentState = State.READ_HEADER;
            ret = true;
        } catch (Exception e) {
            out.add(invalidMessage(buf, e));
        }
        return ret;
    }

    private boolean readHeader(ByteBuf buf, List<Object> out) {
        boolean ret = false;
        try {
            State nextState = parseHeaders(buf);
            if (nextState == null) {
                return false;
            }
            currentState = nextState;
            if (nextState == State.SKIP_CONTROL_CHARS) {
                out.add(message);
                out.add(LastSipContent.EMPTY_LAST_CONTENT);
                resetNow();
                return false;
            }
            contentLength = contentLength();
            if (contentLength == 0 || contentLength == -1 && isDecodingRequest()) {
                out.add(message);
                out.add(LastSipContent.EMPTY_LAST_CONTENT);
                resetNow();
                return false;
            }
            assert nextState == State.READ_FIXED_LENGTH_CONTENT;

            out.add(message);

            chunkSize = contentLength;
            ret = true;
        } catch (Exception e) {
            out.add(invalidMessage(buf, e));
        }
        return ret;
    }

    private void readContent(ByteBuf buf, List<Object> out) {
        int readLimit = buf.readableBytes();
        if (readLimit == 0) {
            return;
        }
        int toRead = Math.min(readLimit, maxChunkSize);
        toRead = Math.min(toRead, chunkSize);
        ByteBuf content = buf.readRetainedSlice(toRead);
        chunkSize -= toRead;

        if (chunkSize == 0) {
            out.add(new DefaultLastSipContent(content, validateHeaders));
            resetNow();
        } else {
            out.add(new DefaultSipContent(content));
        }
    }

    private State parseHeaders(ByteBuf buf) {
        final SipMessage message = this.message;
        final SipHeaders headers = message.headers();

        AppendableCharSequence line = headerParser.parse(buf);
        if (line == null) {
            return null;
        }

        while (line.length() > 0) {
            char first = line.charAtUnsafe(0);
            if (name != null && (first == ' ' || first == '\t')) {
                String trimmedLine = line.toString().trim();
                String valueStr = String.valueOf(value);
                value = valueStr + ' ' + trimmedLine;
            } else {
                Optional.ofNullable(name).ifPresent(n -> headers.add(n, value));
                splitHeader(line);
            }

            line = headerParser.parse(buf);
            if (line == null) {
                return null;
            }
        }

        if (name != null) {
            headers.add(name, value);
        }

        name = null;
        value = null;

        SipMessageDecoderResult decoderResult = new SipMessageDecoderResult(lineParser.size, headerParser.size);
        message.setDecoderResult(decoderResult);

        if (isContentAlwaysEmpty(message) || contentLength() < 0) {
            return State.SKIP_CONTROL_CHARS;
        } else {
            return State.READ_FIXED_LENGTH_CONTENT;
        }
    }

    private void splitHeader(AppendableCharSequence line){
        final int length = line.length();
        int nameStart;
        int nameEnd;
        int colonEnd;
        int valueStart;
        int valueEnd;
        // 跳过空白
        nameStart = findNonWhitespace(line, 0, false);
        // 定位header name
        for (nameEnd = nameStart; nameEnd < length; nameEnd++) {
            char c = line.charAtUnsafe(nameEnd);
            if (c == ':' || (!isDecodingRequest() && isOWS(c))) {
                break;
            }
        }

        if (nameEnd == length) {
            throw new IllegalArgumentException("No colon found");
        }

        for (colonEnd = nameEnd; colonEnd < length; colonEnd++) {
            if (line.charAtUnsafe(colonEnd) == ':') {
                colonEnd++;
                break;
            }
        }

        name = line.subStringUnsafe(nameStart, nameEnd);
        // 定位header value
        valueStart = findNonWhitespace(line, colonEnd, true);
        if (valueStart == length) {
            value = EMPTY_VALUE;
        } else {
            valueEnd = findEndOfString(line);
            value = line.subStringUnsafe(valueStart, valueEnd);
        }
    }

    private int contentLength() {
        if (contentLength == Integer.MIN_VALUE) {
            contentLength = SipUtil.getContentLength(message, -1);
        }
        return contentLength;
    }

    private SipMessage invalidMessage(ByteBuf buf, Exception e) {
        currentState = State.BAD_MESSAGE;
        buf.skipBytes(buf.readableBytes());
        if (message == null) {
            message = createInvalidMessage();
        }
        message.setDecoderResult(DecoderResult.failure(e));
        SipMessage ret = message;
        message = null;
        return ret;
    }

    protected abstract boolean isDecodingRequest();
    protected abstract SipMessage createMessage(String[] initialLine) throws Exception;
    protected abstract SipMessage createInvalidMessage();

    /**
     * 切分首行
     * 对于请求：
     * Request-Line  =  Method SP Request-URI SP SIP-Version CRLF
     * 对于响应：
     * Status-Line  =  SIP-Version SP Status-Code SP Reason-Phrase CRLF
     * 所以不论响应还是请求，首行都由三部分组成
     * todo 首行格式校验是不是在这里做？
     *
     * @param line initialLine
     * @return {@code String[]}, size = 3
     */
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

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof SipExpectationFailedEvent
                && currentState == State.READ_FIXED_LENGTH_CONTENT) {
            reset();
        }
        super.userEventTriggered(ctx, evt);
    }

    protected boolean isContentAlwaysEmpty(SipMessage msg) {
        return false;
    }

    public void reset() {
        this.resetRequested = true;
    }

    private void resetNow() {
        this.message = null;
        name = null;
        value = null;
        contentLength = Integer.MIN_VALUE;
        lineParser.reset();
        headerParser.reset();
        resetRequested = false;
        currentState = State.SKIP_CONTROL_CHARS;
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
            super.reset();
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
