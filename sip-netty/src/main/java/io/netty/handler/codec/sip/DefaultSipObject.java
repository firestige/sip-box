package io.netty.handler.codec.sip;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultHttpObject;
import io.netty.util.internal.ObjectUtil;

/**
 * @author firestige
 * @version [version], 2021-08-15
 * @since [version]
 */
public class DefaultSipObject implements SipObject {
    private static final int HASH_CODE_PRIME = 31;
    private DecoderResult decoderResult = DecoderResult.SUCCESS;

    protected DefaultSipObject() {}

    @Override
    public DecoderResult decoderResult() {
        return decoderResult;
    }

    @Override
    public void setDecoderResult(DecoderResult result) {
        this.decoderResult = ObjectUtil.checkNotNull(result, "decoderResult");
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = HASH_CODE_PRIME * result + decoderResult.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DefaultSipObject && decoderResult().equals(((DefaultSipObject) obj).decoderResult());
    }
}
