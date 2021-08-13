package io.netty.handler.codec.sip;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.AsciiString;

import java.util.Optional;

import static io.netty.util.internal.MathUtil.findNextPositivePowerOfTwo;
import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public class SipMethod implements Comparable<SipMethod> {
    // REGISTER for
    //           registering contact information, INVITE, ACK, and CANCEL for
    //           setting up sessions, BYE for terminating sessions, and
    //           OPTIONS for querying servers about their capabilities.  SIP
    //           extensions, documented in standards track RFCs, may define
    //           additional methods.

    /**
     * {@code Register}
     * RFC3261
     */
    public static final SipMethod REGISTER = new SipMethod("REGISTER");
    /**
     * {@code Invite}
     * RFC3261
     */
    public static final SipMethod INVITE = new SipMethod("INVITE");
    /**
     * {@code Ack}
     * RFC3261
     */
    public static final SipMethod ACK = new SipMethod("ACK");
    /**
     * {@code Bye}
     * RFC3261
     */
    public static final SipMethod BYE = new SipMethod("BYE");
    /**
     * {@code Cancel}
     * RFC3261
     */
    public static final SipMethod CANCEL = new SipMethod("CANCEL");
    /**
     * {@code Update}
     * RFC3311
     */
    public static final SipMethod UPDATE = new SipMethod("UPDATE");
    /**
     * {@code Refer}
     * RFC3515
     */
    public static final SipMethod REFER = new SipMethod("REFER");
    /**
     * {@code Prack}
     * RFC3262
     */
    public static final SipMethod PRACK = new SipMethod("PRACK");
    /**
     * {@code Subscribe}
     * RFC6665
     */
    public static final SipMethod SUBSCRIBE = new SipMethod("SUBSCRIBE");
    /**
     * {@code Notify}
     * RFC6665
     */
    public static final SipMethod NOTIFY = new SipMethod("NOTIFY");
    /**
     * {@code Publish}
     * RFC3903
     */
    public static final SipMethod PUBLISH = new SipMethod("PUBLISH");
    /**
     * {@code Message}
     * RFC3428
     */
    public static final SipMethod MESSAGE = new SipMethod("MESSAGE");
    /**
     * {@code Info}
     * RFC6086
     */
    public static final SipMethod INFO = new SipMethod("INFO");
    /**
     * {@code Options}
     * RFC3261
     */
    public static final SipMethod OPTIONS = new SipMethod("OPTIONS");

    private static final EnumNameMap<SipMethod> methodMap;

    static {
        methodMap = new EnumNameMap<>(
                new EnumNameMap.Node<>(REGISTER.toString(), REGISTER),
                new EnumNameMap.Node<>(INVITE.toString(), INVITE),
                new EnumNameMap.Node<>(ACK.toString(), ACK),
                new EnumNameMap.Node<>(BYE.toString(), BYE),
                new EnumNameMap.Node<>(CANCEL.toString(), CANCEL),
                new EnumNameMap.Node<>(UPDATE.toString(), UPDATE),
                new EnumNameMap.Node<>(REFER.toString(), REFER),
                new EnumNameMap.Node<>(PRACK.toString(), PRACK),
                new EnumNameMap.Node<>(SUBSCRIBE.toString(), SUBSCRIBE),
                new EnumNameMap.Node<>(NOTIFY.toString(), NOTIFY),
                new EnumNameMap.Node<>(PUBLISH.toString(), PUBLISH),
                new EnumNameMap.Node<>(MESSAGE.toString(), MESSAGE),
                new EnumNameMap.Node<>(INFO.toString(), INFO),
                new EnumNameMap.Node<>(OPTIONS.toString(), OPTIONS)
        );
    }

    public static SipMethod valueOf(String name) {
        return Optional.ofNullable(methodMap.get(name))
                .orElseGet(() -> new SipMethod(name));
    }

    private final AsciiString name;

    public SipMethod(String name) {
        name = checkNotNull(name, "name").trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }

        for (int i = 0; i < name.length(); i ++) {
            char c = name.charAt(i);
            if (Character.isISOControl(c) || Character.isWhitespace(c)) {
                throw new IllegalArgumentException("invalid character in name");
            }
        }

        this.name = AsciiString.cached(name);
    }


    /**
     * Returns the name of this method.
     */
    public String name() {
        return name.toString();
    }

    /**
     * Returns the name of this method.
     */
    public AsciiString asciiName() {
        return name;
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }

    @Override
    public String toString() {
        return name.toString();
    }

    @Override
    public int compareTo(SipMethod o) {
        if (o == this) {
            return 0;
        }
        return name().compareTo(o.name());
    }

    private static final class EnumNameMap<T> {
        private final SipMethod.EnumNameMap.Node<T>[] values;
        private final int valuesMask;

        EnumNameMap(EnumNameMap.Node<T>... nodes) {
            values = (EnumNameMap.Node<T>[]) new EnumNameMap.Node[findNextPositivePowerOfTwo(nodes.length)];
            valuesMask = values.length - 1;
            for (SipMethod.EnumNameMap.Node<T> node : nodes) {
                int i = hashCode(node.key) & valuesMask;
                if (values[i] != null) {
                    throw new IllegalArgumentException("index " + i + " collision between values: [" +
                            values[i].key + ", " + node.key + ']');
                }
                values[i] = node;
            }
        }

        T get(String name) {
            EnumNameMap.Node<T> node = values[hashCode(name) & valuesMask];
            return node == null || !node.key.equals(name) ? null : node.value;
        }

        private static int hashCode(String name) {
            // This hash code needs to produce a unique index in the "values" array for each HttpMethod. If new
            // HttpMethods are added this algorithm will need to be adjusted. The constructor will "fail fast" if there
            // are duplicates detected.
            // For example with the current set of HttpMethods it just so happens that the String hash code value
            // shifted right by 6 bits modulo 16 is unique relative to all other HttpMethod values.
            return name.hashCode() >>> 6;
        }

        private static final class Node<T> {
            final String key;
            final T value;

            Node(String key, T value) {
                this.key = key;
                this.value = value;
            }
        }
    }

}
