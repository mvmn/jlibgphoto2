package x.mvmn.jlibgphoto2.exception;

public class GP2Exception extends RuntimeException {

	private static final long serialVersionUID = 1031237231932398086L;

	protected final int code;

	public GP2Exception(final int code) {
		super("GP2 error code: " + code);
		this.code = code;
	}

	public GP2Exception(final int code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message + ". GP2 error code: " + code, cause, enableSuppression, writableStackTrace);
		this.code = code;
	}

	public GP2Exception(final int code, String message, Throwable cause) {
		super(message + ". GP2 error code: " + code, cause);
		this.code = code;
	}

	public GP2Exception(final int code, String message) {
		super(message + ". GP2 error code: " + code);
		this.code = code;
	}

	public GP2Exception(final int code, Throwable cause) {
		super("GP2 error code: " + code, cause);
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
