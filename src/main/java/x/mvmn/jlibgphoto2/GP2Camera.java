package x.mvmn.jlibgphoto2;

import java.util.List;

import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import x.mvmn.gphoto2.jna.Camera;
import x.mvmn.gphoto2.jna.Gphoto2Library;
import x.mvmn.jlibgphoto2.GP2DetectedCamerasListHelper.CameraListItem;
import x.mvmn.jlibgphoto2.util.GP2ErrorHelper;

public class GP2Camera implements AutoCloseable {

	public static void main(String args[]) {
		GP2Context context = new GP2Context();
		List<CameraListItem> detectedCameras = GP2DetectedCamerasListHelper.autodetectCameras(context);
		GP2PortInfoList portList = new GP2PortInfoList();
		GP2Camera camera = new GP2Camera(context, portList.getByPath(detectedCameras.iterator().next().getPortName()));
		System.out.println("Preview file size: " + camera.capturePreview().length);
		camera.close();
		portList.close();
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

		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_init(this.cameraByReference, gp2Context.getPointer()));
	}

	Camera.ByReference getCameraByReference() {
		return cameraByReference;
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

	protected void checkClosed() {
		if (this.closed) {
			throw new RuntimeException("This GP2Camera instance has already been closed.");
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
	public void release() {
		checkClosed();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_exit(cameraByReference, gp2Context.getPointer()));
	}

	public byte[] capturePreview() {
		checkClosed();

		PointerByReference pbrFile = internalCapturePreview();
		byte[] result = internalGetCameraFileData(pbrFile);
		internalFreeCamFileSafely(pbrFile);
		return result;
	}

	protected PointerByReference internalCapturePreview() {
		PointerByReference pbrFile = new PointerByReference();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_file_new(pbrFile));
		pbrFile.setPointer(pbrFile.getValue());
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_capture_preview(cameraByReference, pbrFile, gp2Context.getPointer()));
		return pbrFile;
	}

	protected byte[] internalGetCameraFileData(PointerByReference cameraFile) {
		PointerByReference pref = new PointerByReference();
		LongByReference longByRef = new LongByReference();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_file_get_data_and_size(cameraFile, pref, longByRef));
		return pref.getValue().getByteArray(0, (int) longByRef.getValue());
	}

	protected void internalFreeCamFileSafely(PointerByReference pbrFile) {
		Gphoto2Library.INSTANCE.gp_file_unref(pbrFile);
	}
}
