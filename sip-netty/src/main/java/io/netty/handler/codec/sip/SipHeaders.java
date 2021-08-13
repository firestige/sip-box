package io.netty.handler.codec.sip;

import io.netty.handler.codec.Headers;
import io.netty.handler.codec.HeadersUtils;
import io.netty.util.AsciiString;
import io.netty.util.internal.ObjectUtil;


import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.netty.util.AsciiString.contentEquals;
import static io.netty.util.AsciiString.contentEqualsIgnoreCase;
import static io.netty.util.AsciiString.trim;
import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public abstract class SipHeaders implements Iterable<Map.Entry<String, String>> {
    protected SipHeaders() {}

    /**
     * @see #get(CharSequence)
     */
    public abstract String get(String name);

    /**
     * Returns the value of a header with the specified name.  If there are
     * more than one values for the specified name, the first value is returned.
     *
     * @param name The name of the header to search
     * @return The first header value or {@code null} if there is no such header
     * @see #getAsString(CharSequence)
     */
    public String get(CharSequence name) {
        return get(name.toString());
    }

    /**
     * Returns the value of a header with the specified name.  If there are
     * more than one values for the specified name, the first value is returned.
     *
     * @param name The name of the header to search
     * @return The first header value or {@code defaultValue} if there is no such header
     */
    public String get(CharSequence name, String defaultValue) {
        String value = get(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Returns the integer value of a header with the specified name. If there are more than one values for the
     * specified name, the first value is returned.
     *
     * @param name the name of the header to search
     * @return the first header value if the header is found and its value is an integer. {@code null} if there's no
     *         such header or its value is not an integer.
     */
    public abstract Integer getInt(CharSequence name);

    /**
     * Returns the integer value of a header with the specified name. If there are more than one values for the
     * specified name, the first value is returned.
     *
     * @param name the name of the header to search
     * @param defaultValue the default value
     * @return the first header value if the header is found and its value is an integer. {@code defaultValue} if
     *         there's no such header or its value is not an integer.
     */
    public abstract int getInt(CharSequence name, int defaultValue);

    /**
     * Returns the short value of a header with the specified name. If there are more than one values for the
     * specified name, the first value is returned.
     *
     * @param name the name of the header to search
     * @return the first header value if the header is found and its value is a short. {@code null} if there's no
     *         such header or its value is not a short.
     */
    public abstract Short getShort(CharSequence name);

    /**
     * Returns the short value of a header with the specified name. If there are more than one values for the
     * specified name, the first value is returned.
     *
     * @param name the name of the header to search
     * @param defaultValue the default value
     * @return the first header value if the header is found and its value is a short. {@code defaultValue} if
     *         there's no such header or its value is not a short.
     */
    public abstract short getShort(CharSequence name, short defaultValue);

    /**
     * Returns the date value of a header with the specified name. If there are more than one values for the
     * specified name, the first value is returned.
     *
     * @param name the name of the header to search
     * @return the first header value if the header is found and its value is a date. {@code null} if there's no
     *         such header or its value is not a date.
     */
    public abstract Long getTimeMillis(CharSequence name);

    /**
     * Returns the date value of a header with the specified name. If there are more than one values for the
     * specified name, the first value is returned.
     *
     * @param name the name of the header to search
     * @param defaultValue the default value
     * @return the first header value if the header is found and its value is a date. {@code defaultValue} if
     *         there's no such header or its value is not a date.
     */
    public abstract long getTimeMillis(CharSequence name, long defaultValue);

    /**
     * @see #getAll(CharSequence)
     */
    public abstract List<String> getAll(String name);

    /**
     * Returns the values of headers with the specified name
     *
     * @param name The name of the headers to search
     * @return A {@link List} of header values which will be empty if no values
     *         are found
     * @see #getAllAsString(CharSequence)
     */
    public List<String> getAll(CharSequence name) {
        return getAll(name.toString());
    }

    /**
     * Returns a new {@link List} that contains all headers in this object.  Note that modifying the
     * returned {@link List} will not affect the state of this object.  If you intend to enumerate over the header
     * entries only, use {@link #iterator()} instead, which has much less overhead.
     * @see #iteratorCharSequence()
     */
    public abstract List<Map.Entry<String, String>> entries();

    /**
     * @see #contains(CharSequence)
     */
    public abstract boolean contains(String name);

    /**
     * @return Iterator over the name/value header pairs.
     */
    public abstract Iterator<Map.Entry<CharSequence, CharSequence>> iteratorCharSequence();

    /**
     * Equivalent to {@link #getAll(String)} but it is possible that no intermediate list is generated.
     * @param name the name of the header to retrieve
     * @return an {@link Iterator} of header values corresponding to {@code name}.
     */
    public Iterator<String> valueStringIterator(CharSequence name) {
        return getAll(name).iterator();
    }

    /**
     * Equivalent to {@link #getAll(String)} but it is possible that no intermediate list is generated.
     * @param name the name of the header to retrieve
     * @return an {@link Iterator} of header values corresponding to {@code name}.
     */
    public Iterator<? extends CharSequence> valueCharSequenceIterator(CharSequence name) {
        return valueStringIterator(name);
    }

    /**
     * Checks to see if there is a header with the specified name
     *
     * @param name The name of the header to search for
     * @return True if at least one header is found
     */
    public boolean contains(CharSequence name) {
        return contains(name.toString());
    }

    /**
     * Checks if no header exists.
     */
    public abstract boolean isEmpty();

    /**
     * Returns the number of headers in this object.
     */
    public abstract int size();

    /**
     * Returns a new {@link Set} that contains the names of all headers in this object.  Note that modifying the
     * returned {@link Set} will not affect the state of this object.  If you intend to enumerate over the header
     * entries only, use {@link #iterator()} instead, which has much less overhead.
     */
    public abstract Set<String> names();

    /**
     * @see #add(CharSequence, Object)
     */
    public abstract SipHeaders add(String name, Object value);

    /**
     * Adds a new header with the specified name and value.
     *
     * If the specified value is not a {@link String}, it is converted
     * into a {@link String} by {@link Object#toString()}, except in the cases
     * of {@link Date} and {@link Calendar}, which are formatted to the date
     * format defined in <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3.1">RFC2616</a>.
     *
     * @param name The name of the header being added
     * @param value The value of the header being added
     *
     * @return {@code this}
     */
    public SipHeaders add(CharSequence name, Object value) {
        return add(name.toString(), value);
    }

    /**
     * @see #add(CharSequence, Iterable)
     */
    public abstract SipHeaders add(String name, Iterable<?> values);

    /**
     * Adds a new header with the specified name and values.
     *
     * This getMethod can be represented approximately as the following code:
     * <pre>
     * for (Object v: values) {
     *     if (v == null) {
     *         break;
     *     }
     *     headers.add(name, v);
     * }
     * </pre>
     *
     * @param name The name of the headers being set
     * @param values The values of the headers being set
     * @return {@code this}
     */
    public SipHeaders add(CharSequence name, Iterable<?> values) {
        return add(name.toString(), values);
    }

    /**
     * Adds all header entries of the specified {@code headers}.
     *
     * @return {@code this}
     */
    public SipHeaders add(SipHeaders headers) {
        ObjectUtil.checkNotNull(headers, "headers");
        for (Map.Entry<String, String> e: headers) {
            add(e.getKey(), e.getValue());
        }
        return this;
    }

    /**
     * Add the {@code name} to {@code value}.
     * @param name The name to modify
     * @param value The value
     * @return {@code this}
     */
    public abstract SipHeaders addInt(CharSequence name, int value);

    /**
     * Add the {@code name} to {@code value}.
     * @param name The name to modify
     * @param value The value
     * @return {@code this}
     */
    public abstract SipHeaders addShort(CharSequence name, short value);

    /**
     * @see #set(CharSequence, Object)
     */
    public abstract SipHeaders set(String name, Object value);

    /**
     * Sets a header with the specified name and value.
     *
     * If there is an existing header with the same name, it is removed.
     * If the specified value is not a {@link String}, it is converted into a
     * {@link String} by {@link Object#toString()}, except for {@link Date}
     * and {@link Calendar}, which are formatted to the date format defined in
     * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3.1">RFC2616</a>.
     *
     * @param name The name of the header being set
     * @param value The value of the header being set
     * @return {@code this}
     */
    public SipHeaders set(CharSequence name, Object value) {
        return set(name.toString(), value);
    }

    /**
     * @see #set(CharSequence, Iterable)
     */
    public abstract SipHeaders set(String name, Iterable<?> values);

    /**
     * Sets a header with the specified name and values.
     *
     * If there is an existing header with the same name, it is removed.
     * This getMethod can be represented approximately as the following code:
     * <pre>
     * headers.remove(name);
     * for (Object v: values) {
     *     if (v == null) {
     *         break;
     *     }
     *     headers.add(name, v);
     * }
     * </pre>
     *
     * @param name The name of the headers being set
     * @param values The values of the headers being set
     * @return {@code this}
     */
    public SipHeaders set(CharSequence name, Iterable<?> values) {
        return set(name.toString(), values);
    }

    /**
     * Cleans the current header entries and copies all header entries of the specified {@code headers}.
     *
     * @return {@code this}
     */
    public SipHeaders set(SipHeaders headers) {
        checkNotNull(headers, "headers");

        clear();

        if (headers.isEmpty()) {
            return this;
        }

        for (Map.Entry<String, String> entry : headers) {
            add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Retains all current headers but calls {@link #set(String, Object)} for each entry in {@code headers}
     *
     * @param headers The headers used to {@link #set(String, Object)} values in this instance
     * @return {@code this}
     */
    public SipHeaders setAll(SipHeaders headers) {
        checkNotNull(headers, "headers");

        if (headers.isEmpty()) {
            return this;
        }

        for (Map.Entry<String, String> entry : headers) {
            set(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Set the {@code name} to {@code value}. This will remove all previous values associated with {@code name}.
     * @param name The name to modify
     * @param value The value
     * @return {@code this}
     */
    public abstract SipHeaders setInt(CharSequence name, int value);

    /**
     * Set the {@code name} to {@code value}. This will remove all previous values associated with {@code name}.
     * @param name The name to modify
     * @param value The value
     * @return {@code this}
     */
    public abstract SipHeaders setShort(CharSequence name, short value);

    /**
     * @see #remove(CharSequence)
     */
    public abstract SipHeaders remove(String name);

    /**
     * Removes the header with the specified name.
     *
     * @param name The name of the header to remove
     * @return {@code this}
     */
    public SipHeaders remove(CharSequence name) {
        return remove(name.toString());
    }

    /**
     * Removes all headers from this {@link SipMessage}.
     *
     * @return {@code this}
     */
    public abstract SipHeaders clear();

    /**
     * @see #contains(CharSequence, CharSequence, boolean)
     */
    public boolean contains(String name, String value, boolean ignoreCase) {
        Iterator<String> valueIterator = valueStringIterator(name);
        if (ignoreCase) {
            while (valueIterator.hasNext()) {
                if (valueIterator.next().equalsIgnoreCase(value)) {
                    return true;
                }
            }
        } else {
            while (valueIterator.hasNext()) {
                if (valueIterator.next().equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if a header with the {@code name} and {@code value} exists, {@code false} otherwise.
     * This also handles multiple values that are separated with a {@code ,}.
     * <p>
     * If {@code ignoreCase} is {@code true} then a case insensitive compare is done on the value.
     * @param name the name of the header to find
     * @param value the value of the header to find
     * @param ignoreCase {@code true} then a case insensitive compare is run to compare values.
     * otherwise a case sensitive compare is run to compare values.
     */
    public boolean containsValue(CharSequence name, CharSequence value, boolean ignoreCase) {
        Iterator<? extends CharSequence> itr = valueCharSequenceIterator(name);
        while (itr.hasNext()) {
            if (containsCommaSeparatedTrimmed(itr.next(), value, ignoreCase)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsCommaSeparatedTrimmed(CharSequence rawNext, CharSequence expected,
                                                         boolean ignoreCase) {
        int begin = 0;
        int end;
        if (ignoreCase) {
            if ((end = AsciiString.indexOf(rawNext, ',', begin)) == -1) {
                if (contentEqualsIgnoreCase(trim(rawNext), expected)) {
                    return true;
                }
            } else {
                do {
                    if (contentEqualsIgnoreCase(trim(rawNext.subSequence(begin, end)), expected)) {
                        return true;
                    }
                    begin = end + 1;
                } while ((end = AsciiString.indexOf(rawNext, ',', begin)) != -1);

                if (begin < rawNext.length()) {
                    if (contentEqualsIgnoreCase(trim(rawNext.subSequence(begin, rawNext.length())), expected)) {
                        return true;
                    }
                }
            }
        } else {
            if ((end = AsciiString.indexOf(rawNext, ',', begin)) == -1) {
                if (contentEquals(trim(rawNext), expected)) {
                    return true;
                }
            } else {
                do {
                    if (contentEquals(trim(rawNext.subSequence(begin, end)), expected)) {
                        return true;
                    }
                    begin = end + 1;
                } while ((end = AsciiString.indexOf(rawNext, ',', begin)) != -1);

                if (begin < rawNext.length()) {
                    if (contentEquals(trim(rawNext.subSequence(begin, rawNext.length())), expected)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * {@link Headers#get(Object)} and convert the result to a {@link String}.
     * @param name the name of the header to retrieve
     * @return the first header value if the header is found. {@code null} if there's no such header.
     */
    public final String getAsString(CharSequence name) {
        return get(name);
    }

    /**
     * {@link Headers#getAll(Object)} and convert each element of {@link List} to a {@link String}.
     * @param name the name of the header to retrieve
     * @return a {@link List} of header values or an empty {@link List} if no values are found.
     */
    public final List<String> getAllAsString(CharSequence name) {
        return getAll(name);
    }

    /**
     * {@link Iterator} that converts each {@link Map.Entry}'s key and value to a {@link String}.
     */
    public final Iterator<Map.Entry<String, String>> iteratorAsString() {
        return iterator();
    }

    /**
     * Returns {@code true} if a header with the {@code name} and {@code value} exists, {@code false} otherwise.
     * <p>
     * If {@code ignoreCase} is {@code true} then a case insensitive compare is done on the value.
     * @param name the name of the header to find
     * @param value the value of the header to find
     * @param ignoreCase {@code true} then a case insensitive compare is run to compare values.
     * otherwise a case sensitive compare is run to compare values.
     */
    public boolean contains(CharSequence name, CharSequence value, boolean ignoreCase) {
        return contains(name.toString(), value.toString(), ignoreCase);
    }

    @Override
    public String toString() {
        return HeadersUtils.toString(getClass(), iteratorCharSequence(), size());
    }

    /**
     * Returns a deep copy of the passed in {@link SipHeaders}.
     */
    public SipHeaders copy() {
        return new DefaultSipHeaders().set(this);
    }

}
