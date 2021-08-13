package io.netty.handler.codec.sip;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * todo 弄明白干啥的，看看能不能替换掉
 *
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public class EmptySipHeaders extends SipHeaders {

    static final Iterator<Map.Entry<CharSequence, CharSequence>> EMPTY_CHARS_ITERATOR =
            Collections.<Map.Entry<CharSequence, CharSequence>>emptyList().iterator();

    public static final EmptySipHeaders INSTANCE = instance();

    /** @deprecated */
    @Deprecated
    static EmptySipHeaders instance() {
        return EmptySipHeaders.InstanceInitializer.EMPTY_HEADERS;
    }

    @Override
    public String get(String name) {
        return null;
    }

    @Override
    public Integer getInt(CharSequence name) {
        return null;
    }

    @Override
    public int getInt(CharSequence name, int defaultValue) {
        return 0;
    }

    @Override
    public Short getShort(CharSequence name) {
        return null;
    }

    @Override
    public short getShort(CharSequence name, short defaultValue) {
        return 0;
    }

    @Override
    public Long getTimeMillis(CharSequence name) {
        return null;
    }

    @Override
    public long getTimeMillis(CharSequence name, long defaultValue) {
        return 0;
    }

    @Override
    public List<String> getAll(String name) {
        return null;
    }

    @Override
    public List<Map.Entry<String, String>> entries() {
        return null;
    }

    @Override
    public boolean contains(String name) {
        return false;
    }

    @Override
    public Iterator<Map.Entry<CharSequence, CharSequence>> iteratorCharSequence() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Set<String> names() {
        return null;
    }

    @Override
    public SipHeaders add(String name, Object value) {
        return null;
    }

    @Override
    public SipHeaders add(String name, Iterable<?> values) {
        return null;
    }

    @Override
    public SipHeaders addInt(CharSequence name, int value) {
        return null;
    }

    @Override
    public SipHeaders addShort(CharSequence name, short value) {
        return null;
    }

    @Override
    public SipHeaders set(String name, Object value) {
        return null;
    }

    @Override
    public SipHeaders set(String name, Iterable<?> values) {
        return null;
    }

    @Override
    public SipHeaders setInt(CharSequence name, int value) {
        return null;
    }

    @Override
    public SipHeaders setShort(CharSequence name, short value) {
        return null;
    }

    @Override
    public SipHeaders remove(String name) {
        return null;
    }

    @Override
    public SipHeaders clear() {
        return null;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return null;
    }


    /**
     * This class is needed to break a cyclic static initialization loop between {@link SipHeaders} and
     * {@link EmptySipHeaders}.
     */
    @Deprecated
    private static final class InstanceInitializer {
        /**
         * The instance is instantiated here to break the cyclic static initialization between {@link EmptySipHeaders}
         * and {@link SipHeaders}. The issue is that if someone accesses {@link EmptySipHeaders#INSTANCE} before
         * {@link SipHeaders#EMPTY_HEADERS} then {@link SipHeaders#EMPTY_HEADERS} will be {@code null}.
         */
        @Deprecated
        private static final EmptySipHeaders EMPTY_HEADERS = new EmptySipHeaders();

        private InstanceInitializer() {
        }
    }
}
