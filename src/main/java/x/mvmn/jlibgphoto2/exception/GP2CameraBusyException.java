package x.mvmn.jlibgphoto2.exception;

import x.mvmn.gphoto2.jna.Gphoto2Library;

public class GP2CameraBusyException extends GP2Exception {
	private static final long serialVersionUID = -281265087474549551L;

	public GP2CameraBusyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(Gphoto2Library.GP_ERROR_CAMERA_BUSY, message, cause, enableSuppression, writableStackTrace);
	}

	public GP2CameraBusyException(String message, Throwable cause) {
		super(Gphoto2Library.GP_ERROR_CAMERA_BUSY, message, cause);
	}

	public GP2CameraBusyException(String message) {
		super(Gphoto2Library.GP_ERROR_CAMERA_BUSY, message);
	}

	public GP2CameraBusyException(Throwable cause) {
		super(Gphoto2Library.GP_ERROR_CAMERA_BUSY, cause);
	}

	public GP2CameraBusyException() {
		super(Gphoto2Library.GP_ERROR_CAMERA_BUSY);
	}
}
