package io.netty.handler.codec.sip;

import io.netty.handler.codec.DefaultHeaders;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.ValueConverter;
import io.netty.util.HashingStrategy;
import io.netty.util.internal.StringUtil;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.netty.util.AsciiString.CASE_INSENSITIVE_HASHER;
import static io.netty.util.internal.StringUtil.COMMA;
import static io.netty.util.internal.StringUtil.unescapeCsvFields;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public class CombinedSipHeaders extends DefaultSipHeaders {
    public CombinedSipHeaders(boolean validate) {
        super(new CombinedSipHeadersImpl(CASE_INSENSITIVE_HASHER, valueConverter(validate), nameValidator(validate)));
    }

    @Override
    public boolean containsValue(CharSequence name, CharSequence value, boolean ignoreCase) {
        return super.containsValue(name, StringUtil.trimOws(value), ignoreCase);
    }

    private static final class CombinedSipHeadersImpl extends DefaultHeaders<CharSequence, CharSequence,
            CombinedSipHeadersImpl> {
        private static final int VALUE_LENGTH_ESTIMATE = 10;
        private CsvValueEscaper<Object> objectEscaper;
        private CsvValueEscaper<CharSequence> charSequenceEscaper;

        CombinedSipHeadersImpl(HashingStrategy<CharSequence> nameHashingStrategy,
                               ValueConverter<CharSequence> valueConverter,
                               NameValidator<CharSequence> nameValidator) {
            super(nameHashingStrategy, valueConverter, nameValidator);
        }


        @Override
        public Iterator<CharSequence> valueIterator(CharSequence name) {
            Iterator<CharSequence> iter = super.valueIterator(name);
            if (!iter.hasNext() || cannotBeCombined(name)) {
                return iter;
            }
            Iterator<CharSequence> unescapedIter = unescapeCsvFields(iter.next()).iterator();
            if (iter.hasNext()) {
                throw new IllegalStateException("CombinedSipHeaders should only have one value");
            }
            return unescapedIter;
        }

        @Override
        public List<CharSequence> getAll(CharSequence name) {
            List<CharSequence> values = super.getAll(name);
            if (values.isEmpty() || cannotBeCombined(name)) {
                return values;
            }
            if (values.size() != 1) {
                throw new IllegalStateException("CombinedSipHeaders should only have one value");
            }
            return unescapeCsvFields(values.get(0));
        }

        @Override
        public CombinedSipHeadersImpl add(Headers<? extends CharSequence, ? extends CharSequence, ?> headers) {
            // Override the fast-copy mechanism used by DefaultHeaders
            if (headers == this) {
                throw new IllegalArgumentException("can't add to itself.");
            }
            if (headers instanceof CombinedSipHeaders.CombinedSipHeadersImpl) {
                if (isEmpty()) {
                    // Can use the fast underlying copy
                    addImpl(headers);
                } else {
                    // Values are already escaped so don't escape again
                    for (Map.Entry<? extends CharSequence, ? extends CharSequence> header : headers) {
                        addEscapedValue(header.getKey(), header.getValue());
                    }
                }
            } else {
                for (Map.Entry<? extends CharSequence, ? extends CharSequence> header : headers) {
                    add(header.getKey(), header.getValue());
                }
            }
            return this;
        }

        @Override
        public CombinedSipHeadersImpl set(Headers<? extends CharSequence, ? extends CharSequence, ?> headers) {
            if (headers == this) {
                return this;
            }
            clear();
            return add(headers);
        }

        @Override
        public CombinedSipHeadersImpl setAll(Headers<? extends CharSequence, ? extends CharSequence, ?> headers) {
            if (headers == this) {
                return this;
            }
            for (CharSequence key : headers.names()) {
                remove(key);
            }
            return add(headers);
        }

        @Override
        public CombinedSipHeadersImpl add(CharSequence name, CharSequence value) {
            return addEscapedValue(name, charSequenceEscaper().escape(value));
        }

        @Override
        public CombinedSipHeadersImpl add(CharSequence name, CharSequence... values) {
            return addEscapedValue(name, commaSeparate(charSequenceEscaper(), values));
        }

        @Override
        public CombinedSipHeadersImpl add(CharSequence name, Iterable<? extends CharSequence> values) {
            return addEscapedValue(name, commaSeparate(charSequenceEscaper(), values));
        }

        @Override
        public CombinedSipHeadersImpl addObject(CharSequence name, Object value) {
            return addEscapedValue(name, commaSeparate(objectEscaper(), value));
        }

        @Override
        public CombinedSipHeadersImpl addObject(CharSequence name, Iterable<?> values) {
            return addEscapedValue(name, commaSeparate(objectEscaper(), values));
        }

        @Override
        public CombinedSipHeadersImpl addObject(CharSequence name, Object... values) {
            return addEscapedValue(name, commaSeparate(objectEscaper(), values));
        }

        @Override
        public CombinedSipHeadersImpl set(CharSequence name, CharSequence... values) {
            super.set(name, commaSeparate(charSequenceEscaper(), values));
            return this;
        }

        @Override
        public CombinedSipHeadersImpl set(CharSequence name, Iterable<? extends CharSequence> values) {
            super.set(name, commaSeparate(charSequenceEscaper(), values));
            return this;
        }

        @Override
        public CombinedSipHeadersImpl setObject(CharSequence name, Object value) {
            super.set(name, commaSeparate(objectEscaper(), value));
            return this;
        }

        @Override
        public CombinedSipHeadersImpl setObject(CharSequence name, Iterable<?> values) {
            super.set(name, commaSeparate(objectEscaper(), values));
            return this;
        }

        private CsvValueEscaper<Object> objectEscaper() {
            return Optional.ofNullable(objectEscaper)
                    .orElseGet(() -> {
                        this.objectEscaper = value ->
                                StringUtil.escapeCsv(valueConverter().convertObject(value), true);
                        return this.objectEscaper;
                    });
        }

        private CsvValueEscaper<CharSequence> charSequenceEscaper() {
            return Optional.ofNullable(charSequenceEscaper)
                    .orElseGet(() -> {
                        this.charSequenceEscaper = value ->
                                StringUtil.escapeCsv(value, true);
                        return this.charSequenceEscaper;
                    });
        }

        /**
         * 不能有多值的header
         * todo 核对标准，Sip应该很多header都不能有多值
         *
         * @param name
         * @return
         */
        private static boolean cannotBeCombined(CharSequence name) {
            return true;
        }

        private CombinedSipHeadersImpl addEscapedValue(CharSequence name, CharSequence escapedValue) {
            CharSequence currentValue = super.get(name);
            if (currentValue == null || cannotBeCombined(name)) {
                super.add(name, escapedValue);
            } else {
                super.set(name, commaSeparateEscapedValues(currentValue, escapedValue));
            }
            return this;
        }

        private static <T> CharSequence commaSeparate(CsvValueEscaper<T> escaper, T... values) {
            StringBuilder sb = new StringBuilder(values.length * VALUE_LENGTH_ESTIMATE);
            if (values.length > 0) {
                int end = values.length - 1;
                for (int i = 0; i < end; i++) {
                    sb.append(escaper.escape(values[i])).append(COMMA);
                }
                sb.append(escaper.escape(values[end]));
            }
            return sb;
        }

        private static <T> CharSequence commaSeparate(CsvValueEscaper<T> escaper, Iterable<? extends T> values) {
            @SuppressWarnings("rawtypes")
            final StringBuilder sb = values instanceof Collection
                    ? new StringBuilder(((Collection) values).size() * VALUE_LENGTH_ESTIMATE) : new StringBuilder();
            Iterator<? extends T> iterator = values.iterator();
            if (iterator.hasNext()) {
                T next = iterator.next();
                while (iterator.hasNext()) {
                    sb.append(escaper.escape(next)).append(COMMA);
                    next = iterator.next();
                }
                sb.append(escaper.escape(next));
            }
            return sb;
        }

        private static CharSequence commaSeparateEscapedValues(CharSequence currentValue, CharSequence value) {
            return new StringBuilder(currentValue.length() + 1 + value.length())
                    .append(currentValue)
                    .append(COMMA)
                    .append(value);
        }

        private interface CsvValueEscaper<T> {
            CharSequence escape(T value);
        }
    }
}
