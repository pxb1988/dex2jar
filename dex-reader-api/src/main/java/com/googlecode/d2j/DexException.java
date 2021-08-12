package com.googlecode.d2j;

/**
 * @author <a href="mailto:pxb1988@gmail.com">Panxiaobo</a>
 * @version $Rev$
 */
public class DexException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 6294916997539922829L;

    /**
     *
     */
    public DexException() {
    }

    /**
     *
     */
    public DexException(String message) {
        super(message);
    }

    /**
     *
     */
    public DexException(Throwable cause) {
        super(cause);
    }

    /**
     *
     */
    public DexException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * this is equals to
     *
     * <b> new DexException(String.format(messageFormat, args), cause); </b>
     */
    public DexException(Throwable cause, String messageFormat, Object... args) {
        this(String.format(messageFormat, args), cause);
    }

    /**
     * this is equals to
     *
     * <b> new DexException(String.format(messageFormat, args)); </b>
     */
    public DexException(String messageFormat, Object... args) {
        this(String.format(messageFormat, args));
    }

}
