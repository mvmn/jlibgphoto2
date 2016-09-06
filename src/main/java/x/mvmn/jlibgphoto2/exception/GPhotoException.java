package x.mvmn.jlibgphoto2.exception;

public class GPhotoException extends RuntimeException {

	private static final long serialVersionUID = 1031237231932398086L;

	public GPhotoException() {
		super();
	}

	public GPhotoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GPhotoException(String message, Throwable cause) {
		super(message, cause);
	}

	public GPhotoException(String message) {
		super(message);
	}

	public GPhotoException(Throwable cause) {
		super(cause);
	}
}
