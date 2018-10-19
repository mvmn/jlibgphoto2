package x.mvmn.jlibgphoto2.api;

import java.util.EnumSet;
import java.util.List;

import x.mvmn.gphoto2.jna.Gphoto2Library.CameraCaptureType;
import x.mvmn.gphoto2.jna.Gphoto2Library.CameraEventType;
import x.mvmn.jlibgphoto2.api.file.CameraFile;

public interface GP2Camera {

	public static enum GP2CameraCaptureType {

		IMAGE(CameraCaptureType.GP_CAPTURE_IMAGE), MOVIE(CameraCaptureType.GP_CAPTURE_MOVIE), SOUND(CameraCaptureType.GP_CAPTURE_SOUND);

		private int code;

		private GP2CameraCaptureType(final int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static GP2CameraCaptureType getByCode(final int code) {
			GP2CameraCaptureType result = null;
			for (GP2CameraCaptureType val : GP2CameraCaptureType.values()) {
				if (val.getCode() == code) {
					result = val;
					break;
				}
			}
			return result;
		}
	}

	public static enum GP2CameraEventType {
		CAPTURE_COMPLETE(CameraEventType.GP_EVENT_CAPTURE_COMPLETE), FILE_ADDED(CameraEventType.GP_EVENT_FILE_ADDED), FOLDER_ADDED(
				CameraEventType.GP_EVENT_FOLDER_ADDED), TIMEOUT(CameraEventType.GP_EVENT_TIMEOUT), UNKNOWN(CameraEventType.GP_EVENT_UNKNOWN);

		private int code;

		private GP2CameraEventType(final int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static GP2CameraEventType getByCode(final int code) {
			GP2CameraEventType result = null;
			for (GP2CameraEventType val : GP2CameraEventType.values()) {
				if (val.getCode() == code) {
					result = val;
					break;
				}
			}
			return result;
		}
	}

	/**
	 * Close connection to camera (see libgphoto2 gp_camera_exit).<br/>
	 * <br/>
	 * Closes a connection to the camera and therefore gives other application the possibility to access the camera, too.<br/>
	 * <br/>
	 * It is recommended that you call this function when you currently don't need the camera. The camera will get reinitialized by gp_camera_init()
	 * automatically if you try to access the camera again.
	 */
	void release();

	byte[] capturePreview();

	CameraFileSystemEntryBean captureImage();

	CameraFileSystemEntryBean capture(GP2CameraCaptureType captureType);

	String getSummary();

	void deleteCameraFile(final String path, final String fileName);

	byte[] getCameraFileContents(final String path, final String fileName, boolean preview);

	byte[] getCameraFileContents(final String path, final String fileName);

	CameraFile getFileInfo(final String path, final String fileName);

	List<CameraFileSystemEntryBean> listCameraFiles(final String path, final boolean inclueFiles, final boolean includeFolders, final boolean recursive);

	List<CameraFileSystemEntryBean> listCameraFiles(final String path, final boolean includeFolders, final boolean recursive);

	List<CameraFileSystemEntryBean> listCameraFiles(final String path, final boolean recursive);

	List<CameraFileSystemEntryBean> listCameraFiles(final String path);

	void setConfig(CameraConfigEntryBean... newValues);

	List<CameraConfigEntryBean> getConfig();

	void close();

	GP2CameraEventType waitForEvent(int timeout);

	GP2CameraEventType waitForSpecificEvent(int timeout, EnumSet<GP2CameraEventType> expectedEventTypes);

	GP2CameraEventType waitForSpecificEvent(int timeout, GP2CameraEventType... expectedEventTypes);
}