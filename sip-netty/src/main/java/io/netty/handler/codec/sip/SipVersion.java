package io.netty.handler.codec.sip;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.ObjectUtil;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sip协议版本
 *
 * @author firestige
 * @version 0.0.1, 2021-08-15
 * @since 0.0.1
 */
public class SipVersion implements Comparable<SipVersion> {
    private static final Pattern VERSION_PATTERN = Pattern.compile("SIP/(\\d)\\.(\\d)");
    private static final String SIP_2_0_STRING = "SIP/2.0";
    public static final SipVersion SIP_2_0 = new SipVersion("SIP", 2, 0, true);
    private final String protocolName;
    private final int majorVersion;
    private final int minorVersion;
    private final String text;
    private final byte[] bytes;

    public static SipVersion valueOf(String text) {
        ObjectUtil.checkNotNull(text, "text");
        text = text.trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("text is empty");
        } else {
            SipVersion version = version0(text);
            if (version == null) {
                version = new SipVersion(text);
            }
            return version;
        }
    }

    private static SipVersion version0(String text) {
        return SIP_2_0_STRING.equals(text) ? SIP_2_0 : null;
    }

    public SipVersion(String text) {
        ObjectUtil.checkNotNull(text, "text");
        text = text.trim().toUpperCase(Locale.ROOT);
        if (text.isEmpty()) {
            throw new IllegalArgumentException("empty text");
        } else {
            Matcher matcher = VERSION_PATTERN.matcher(text);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("invalid version");
            } else {
                this.protocolName = matcher.group(1);
                this.majorVersion = Integer.parseInt(matcher.group(2));
                this.minorVersion = Integer.parseInt(matcher.group(3));
                this.text = this.protocolName + '/' + this.majorVersion + '.' + this.minorVersion;
                this.bytes = null;
            }
        }
    }

    public SipVersion(String protocolName, int majorVersion, int minorVersion) {
        this(protocolName, majorVersion, minorVersion, false);
    }

    private SipVersion(String protocolName, int majorVersion, int minorVersion, boolean bytes) {
        ObjectUtil.checkNotNull(protocolName, "protocolName");
        protocolName = protocolName.trim().toLowerCase(Locale.ROOT);
        if (protocolName.isEmpty()) {
            throw new IllegalArgumentException("empty protocolName");
        } else {
            for (int i = 0; i < protocolName.length(); ++i) {
                if (Character.isISOControl(protocolName.charAt(i)) || Character.isWhitespace(protocolName.charAt(i))) {
                    throw new IllegalArgumentException("invalid character in protocolName");
                }
            }

            ObjectUtil.checkPositive(majorVersion, "majorVersion");
            ObjectUtil.checkPositiveOrZero(minorVersion, "minorVersion");
            this.protocolName = protocolName;
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.text = protocolName + '/' + majorVersion + '.' + minorVersion;
            this.bytes = bytes ? this.text.getBytes(CharsetUtil.US_ASCII) : null;
        }

    }

    public String protocolName() {
        return protocolName;
    }

    public int majorVersion() {
        return majorVersion;
    }

    public int minorVersion() {
        return minorVersion;
    }

    public String text() {
        return text;
    }

    @Override
    public int hashCode() {
        return (this.protocolName().hashCode() * 31 + this.majorVersion()) * 31 + this.minorVersion();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SipVersion && text.equals(((SipVersion) obj).text());
    }

    @Override
    public String toString() {
        return this.text();
    }

    @Override
    public int compareTo(SipVersion o) {
        int v = this.protocolName().compareTo(o.protocolName());
        if (v != 0) {
            return v;
        } else {
            v = this.majorVersion() - o.majorVersion();
            return v != 0 ? v : this.minorVersion() - o.minorVersion();
        }
    }

    void encode(ByteBuf buf) {
        if (this.bytes == null) {
            buf.writeCharSequence(this.text, CharsetUtil.US_ASCII);
        } else {
            buf.writeBytes(bytes);
        }
    }
}
