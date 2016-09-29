package x.mvmn.jlibgphoto2.exception;

import x.mvmn.gphoto2.jna.Gphoto2Library;

public class GPhotoCameraBusyException extends GPhotoException {
	private static final long serialVersionUID = -281265087474549551L;

	public GPhotoCameraBusyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(Gphoto2Library.GP_ERROR_CAMERA_BUSY, message, cause, enableSuppression, writableStackTrace);
	}

	public GPhotoCameraBusyException(String message, Throwable cause) {
		super(Gphoto2Library.GP_ERROR_CAMERA_BUSY, message, cause);
	}

	public GPhotoCameraBusyException(String message) {
		super(Gphoto2Library.GP_ERROR_CAMERA_BUSY, message);
	}

	public GPhotoCameraBusyException(Throwable cause) {
		super(Gphoto2Library.GP_ERROR_CAMERA_BUSY, cause);
	}

	public GPhotoCameraBusyException() {
		super(Gphoto2Library.GP_ERROR_CAMERA_BUSY);
	}
}
