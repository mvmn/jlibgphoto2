package x.mvmn.jlibgphoto2.util;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import x.mvmn.gphoto2.jna.Gphoto2Library;
import x.mvmn.jlibgphoto2.exception.GPhotoCameraBusyExceptoon;
import x.mvmn.jlibgphoto2.exception.GPhotoException;

public class GP2ErrorHelper {

	protected static final Map<Integer, String> ERROR_CODES;

	static {
		final Map<Integer, String> errorCodes = new HashMap<Integer, String>();
		for (Field field : Gphoto2Library.class.getDeclaredFields()) {
			if (field.getName().startsWith("GP_ERROR")) {
				try {
					final String errorName = field.getName();
					final int errorCode = Integer.parseInt(field.get(null).toString());
					errorCodes.put(errorCode, errorName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		ERROR_CODES = Collections.unmodifiableMap(errorCodes);
	}

	public static int checkResult(int result) {
		if (result < Gphoto2Library.GP_OK) {
			String errorName = ERROR_CODES.get(result);
			if (errorName == null) {
				errorName = "Unknown error";
			}
			if (result == Gphoto2Library.GP_ERROR_CAMERA_BUSY) {
				throw new GPhotoCameraBusyExceptoon();
			} else {
				throw new GPhotoException(result, errorName);
			}
		} else {
			return result;
		}
	}
}
