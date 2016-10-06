package x.mvmn.jlibgphoto2;

import java.nio.IntBuffer;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.ptr.PointerByReference;

import x.mvmn.gphoto2.jna.Camera;
import x.mvmn.gphoto2.jna.CameraFilePath;
import x.mvmn.gphoto2.jna.CameraText;
import x.mvmn.gphoto2.jna.Gphoto2Library;
import x.mvmn.gphoto2.jna.Gphoto2Library.CameraCaptureType;
import x.mvmn.gphoto2.jna.Gphoto2Library.CameraEventType;
import x.mvmn.jlibgphoto2.GP2AutodetectCameraHelper.CameraListItemBean;
import x.mvmn.jlibgphoto2.util.GP2ErrorHelper;

public class GP2Camera implements AutoCloseable {

	protected static final String NATIVE_STRING_ENCODING = System.getProperty("jlibgphoto2.camerafilepath.encoding", "ASCII");

	public static void main(String args[]) {
		GP2Context context = new GP2Context();
		List<CameraListItemBean> detectedCameras = GP2AutodetectCameraHelper.autodetectCameras(context);
		GP2PortInfoList portList = new GP2PortInfoList();
		GP2Camera camera = new GP2Camera(context, portList.getByPath(detectedCameras.iterator().next().getPortName()));
		System.out.println(camera.getSummary());
		System.out.println("Preview file size: " + camera.capturePreview().length);
		System.out.println(camera.capture());
		camera.close();
		portList.close();
	}

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

	protected final GP2Context gp2Context;
	protected final Camera.ByReference cameraByReference;
	protected volatile boolean closed = false;

	public GP2Camera() {
		this(new GP2Context(), null);
	}

	public GP2Camera(final GP2Context gp2Context) {
		this(gp2Context, null);
	}

	public GP2Camera(final GP2PortInfoList.GP2PortInfo gp2PortInfo) {
		this(new GP2Context(), gp2PortInfo);
	}

	public GP2Camera(final GP2Context gp2Context, final GP2PortInfoList.GP2PortInfo gp2PortInfo) {
		this.gp2Context = gp2Context;

		Camera.ByReference[] p2CamByRef = new Camera.ByReference[] { new Camera.ByReference() };
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_new(p2CamByRef));
		this.cameraByReference = p2CamByRef[0];

		if (gp2PortInfo != null) {
			GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_set_port_info(this.cameraByReference, gp2PortInfo.getGpPortInfo()));
		}

		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_init(this.cameraByReference, gp2Context.getPointerByRef()));
	}

	Camera.ByReference getCameraByReference() {
		return cameraByReference;
	}

	protected void checkClosed() {
		if (this.closed) {
			throw new RuntimeException("This GP2Camera instance has already been closed.");
		}
	}

	protected PointerByReference internalCapturePreview() {
		PointerByReference pbrFile = new PointerByReference();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_file_new(pbrFile));
		pbrFile.setPointer(pbrFile.getValue());
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_capture_preview(cameraByReference, pbrFile, gp2Context.getPointerByRef()));
		return pbrFile;
	}

	public GP2Context getContext() {
		checkClosed();
		return gp2Context;
	}

	public void close() {
		// checkClosed(); // happens in release();
		release();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_unref(cameraByReference));
		this.closed = true;
	}

	/**
	 * Close connection to camera (see libgphoto2 gp_camera_exit).<br/>
	 * <br/>
	 * Closes a connection to the camera and therefore gives other application the possibility to access the camera, too.<br/>
	 * <br/>
	 * It is recommended that you call this function when you currently don't need the camera. The camera will get reinitialized by gp_camera_init()
	 * automatically if you try to access the camera again.
	 */
	public void release() {
		checkClosed();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_exit(cameraByReference, gp2Context.getPointerByRef()));
	}

	public byte[] capturePreview() {
		checkClosed();

		PointerByReference pbrFile = internalCapturePreview();
		byte[] result = GP2CameraFilesHelper.internalGetCameraFileData(pbrFile);
		GP2CameraFilesHelper.internalFreeCameraFileSafely(pbrFile);
		return result;
	}

	public CameraFileSystemEntryBean capture() {
		return capture(GP2CameraCaptureType.IMAGE);
	}

	public CameraFileSystemEntryBean capture(final GP2CameraCaptureType captureType) {
		CameraFilePath.ByReference refCameraFilePath = new CameraFilePath.ByReference();
		GP2ErrorHelper.checkResult(
				Gphoto2Library.INSTANCE.gp_camera_capture(cameraByReference, captureType.getCode(), refCameraFilePath, gp2Context.getPointerByRef()));
		return new CameraFileSystemEntryBean(Native.toString(refCameraFilePath.name, NATIVE_STRING_ENCODING),
				Native.toString(refCameraFilePath.folder, NATIVE_STRING_ENCODING), false);
	}

	public String getSummary() {
		CameraText.ByReference byRefCameraText = new CameraText.ByReference();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_get_summary(cameraByReference, byRefCameraText, gp2Context.getPointerByRef()));
		return Native.toString(byRefCameraText.text, NATIVE_STRING_ENCODING);
	}

	public int waitForSpecificEvent(int timeout, GP2CameraEventType expectedEventType) {
		int eventType = -1;
		long startTime = System.currentTimeMillis();
		long timeLeft = timeout;
		do {
			eventType = waitForEvent((int) timeLeft);
			timeLeft = timeout - (System.currentTimeMillis() - startTime);
		} while (timeLeft > 0 && eventType != expectedEventType.getCode());
		return eventType;
	}

	public int waitForEvent(int timeout) {
		PointerByReference eventData = new PointerByReference();
		IntBuffer ibEventType = IntBuffer.allocate(1);
		GP2ErrorHelper.checkResult(
				Gphoto2Library.INSTANCE.gp_camera_wait_for_event(cameraByReference, timeout, ibEventType, eventData, gp2Context.getPointerByRef()));
		int eventType = ibEventType.get(0);
		return eventType;
	}
}
