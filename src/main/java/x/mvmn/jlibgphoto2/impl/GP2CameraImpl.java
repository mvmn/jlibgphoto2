package x.mvmn.jlibgphoto2.impl;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

import x.mvmn.gphoto2.jna.Camera;
import x.mvmn.gphoto2.jna.CameraFileInfo;
import x.mvmn.gphoto2.jna.CameraFilePath;
import x.mvmn.gphoto2.jna.CameraText;
import x.mvmn.gphoto2.jna.Gphoto2Library;
import x.mvmn.gphoto2.jna.Gphoto2Library.CameraFileType;
import x.mvmn.jlibgphoto2.api.CameraConfigEntryBean;
import x.mvmn.jlibgphoto2.api.CameraConfigEntryBean.CameraConfigEntryType;
import x.mvmn.jlibgphoto2.api.CameraConfigEntryBean.Range;
import x.mvmn.jlibgphoto2.api.CameraFileSystemEntryBean;
import x.mvmn.jlibgphoto2.api.CameraListItemBean;
import x.mvmn.jlibgphoto2.api.GP2Camera;
import x.mvmn.jlibgphoto2.util.GP2ErrorHelper;

public class GP2CameraImpl implements GP2Camera {

	public static final String NATIVE_STRING_ENCODING = System.getProperty("jlibgphoto2.stringencoding", "ASCII");

	public static void main(String args[]) {
		GP2Context context = new GP2Context();
		List<CameraListItemBean> detectedCameras = new CameraDetectorImpl().detectCameras(context);
		GP2PortInfoList portList = new GP2PortInfoList();
		GP2Camera camera = new GP2CameraImpl(context, portList.getByPath(detectedCameras.iterator().next().getPortName()));
		System.out.println(camera.getSummary());
		System.out.println("Preview file size: " + camera.capturePreview().length);
		System.out.println(camera.captureImage());
		camera.close();
		portList.close();
	}

	protected final GP2Context gp2Context;
	protected final Camera.ByReference cameraByReference;
	protected volatile boolean closed = false;

	public GP2CameraImpl() {
		this(new GP2Context(), null);
	}

	public GP2CameraImpl(final GP2Context gp2Context) {
		this(gp2Context, null);
	}

	public GP2CameraImpl(final GP2PortInfoList.GP2PortInfo gp2PortInfo) {
		this(new GP2Context(), gp2PortInfo);
	}

	public GP2CameraImpl(final GP2Context gp2Context, final GP2PortInfoList.GP2PortInfo gp2PortInfo) {
		this.gp2Context = gp2Context;

		Camera.ByReference[] p2CamByRef = new Camera.ByReference[] { new Camera.ByReference() };
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_new(p2CamByRef));
		this.cameraByReference = p2CamByRef[0];

		if (gp2PortInfo != null) {
			GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_set_port_info(this.cameraByReference, gp2PortInfo.getGpPortInfo()));
		}

		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_init(this.cameraByReference, gp2Context.getPointerByRef()));
	}

	protected Camera.ByReference getCameraByReference() {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.jlibgphoto2.impl.GP2Camera#getContext()
	 */
	public GP2Context getContext() {
		checkClosed();
		return gp2Context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.jlibgphoto2.impl.GP2Camera#close()
	 */
	public void close() {
		// checkClosed(); // happens in release();
		release();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_unref(cameraByReference));
		this.closed = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.jlibgphoto2.impl.GP2Camera#release()
	 */
	public void release() {
		checkClosed();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_exit(cameraByReference, gp2Context.getPointerByRef()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.jlibgphoto2.impl.GP2Camera#capturePreview()
	 */
	public byte[] capturePreview() {
		checkClosed();

		PointerByReference pbrFile = internalCapturePreview();
		byte[] result = internalGetCameraFileData(pbrFile);
		internalFreeCameraFileSafely(pbrFile);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.jlibgphoto2.impl.GP2Camera#capture()
	 */
	public CameraFileSystemEntryBean captureImage() {
		return capture(GP2CameraCaptureType.IMAGE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.jlibgphoto2.impl.GP2Camera#capture(x.mvmn.jlibgphoto2.impl.GP2CameraImpl.GP2CameraCaptureType)
	 */
	public CameraFileSystemEntryBean capture(final GP2CameraCaptureType captureType) {
		CameraFilePath.ByReference refCameraFilePath = new CameraFilePath.ByReference();
		GP2ErrorHelper.checkResult(
				Gphoto2Library.INSTANCE.gp_camera_capture(cameraByReference, captureType.getCode(), refCameraFilePath, gp2Context.getPointerByRef()));
		return new CameraFileSystemEntryBean(Native.toString(refCameraFilePath.name, NATIVE_STRING_ENCODING),
				Native.toString(refCameraFilePath.folder, NATIVE_STRING_ENCODING), false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.jlibgphoto2.impl.GP2Camera#getSummary()
	 */
	public String getSummary() {
		CameraText.ByReference byRefCameraText = new CameraText.ByReference();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_get_summary(cameraByReference, byRefCameraText, gp2Context.getPointerByRef()));
		return Native.toString(byRefCameraText.text, NATIVE_STRING_ENCODING);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.jlibgphoto2.impl.GP2Camera#waitForSpecificEvent(int, x.mvmn.jlibgphoto2.impl.GP2CameraImpl.GP2CameraEventType)
	 */
	public GP2CameraEventType waitForSpecificEvent(int timeout, GP2CameraEventType... expectedEventTypes) {
		return waitForSpecificEvent(timeout, EnumSet.of(expectedEventTypes[0], expectedEventTypes));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.jlibgphoto2.impl.GP2Camera#waitForSpecificEvent(int, java.util.EnumSet)
	 */
	public GP2CameraEventType waitForSpecificEvent(int timeout, EnumSet<GP2CameraEventType> expectedEventTypes) {
		GP2CameraEventType receivedEvent = null;
		final long startTime = System.currentTimeMillis();
		long timeLeft = timeout;
		do {
			receivedEvent = waitForEvent((int) timeLeft);
			timeLeft = timeout - (System.currentTimeMillis() - startTime);
		} while (timeLeft > 0 && expectedEventTypes.contains(receivedEvent));
		return receivedEvent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.jlibgphoto2.impl.GP2Camera#waitForEvent(int)
	 */
	public GP2CameraEventType waitForEvent(int timeout) {
		final PointerByReference eventData = new PointerByReference();
		final IntBuffer ibEventType = IntBuffer.allocate(1);
		GP2ErrorHelper.checkResult(
				Gphoto2Library.INSTANCE.gp_camera_wait_for_event(cameraByReference, timeout, ibEventType, eventData, gp2Context.getPointerByRef()));
		final int eventType = ibEventType.get(0);
		return GP2CameraEventType.getByCode(eventType);
	}

	public List<CameraConfigEntryBean> getConfig() {
		List<CameraConfigEntryBean> result = new ArrayList<CameraConfigEntryBean>();
		PointerByReference pbrCameraConfigRoot = new PointerByReference();
		try {
			GP2ErrorHelper.checkResult(
					Gphoto2Library.INSTANCE.gp_camera_get_config(this.getCameraByReference(), pbrCameraConfigRoot, this.getContext().getPointerByRef()));
			pbrCameraConfigRoot.setPointer(pbrCameraConfigRoot.getValue());
			visitWidgets(pbrCameraConfigRoot, "", result);
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		} finally {
			try {
				GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_free(pbrCameraConfigRoot));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	public void setConfig(CameraConfigEntryBean... newValues) {
		PointerByReference pbrCameraConfigRoot = new PointerByReference();
		GP2ErrorHelper.checkResult(
				Gphoto2Library.INSTANCE.gp_camera_get_config(this.getCameraByReference(), pbrCameraConfigRoot, this.getContext().getPointerByRef()));
		pbrCameraConfigRoot.setPointer(pbrCameraConfigRoot.getValue());
		try {
			for (CameraConfigEntryBean newVal : newValues) {
				PointerByReference pbrCameraConfigWidget = new PointerByReference();
				pbrCameraConfigWidget.setPointer(pbrCameraConfigRoot.getPointer());
				PointerByReference pbrTargetWidget = new PointerByReference();
				for (final String pathElem : newVal.getPath().substring(1).split("/")) {
					GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_child_by_name(pbrCameraConfigWidget, pathElem, pbrTargetWidget));
					pbrCameraConfigWidget.setPointer(pbrTargetWidget.getValue());
				}
				pbrTargetWidget.setPointer(pbrTargetWidget.getValue());
				Pointer valuePtr = null;
				switch (newVal.getType().getValueType()) {
					case STRING:
						if (newVal.getStrValue() == null) {
							valuePtr = null;
						} else {
							final byte[] b;
							try {
								b = newVal.getStrValue().getBytes("ASCII");
							} catch (UnsupportedEncodingException ex) {
								throw new RuntimeException(ex);
							}
							final ByteBuffer buf = ByteBuffer.allocateDirect(b.length + 1);
							buf.put(b);
							valuePtr = Native.getDirectBufferPointer(buf);
						}
					break;
					case INT:
						Integer value = newVal.getIntValue();
						if (newVal.getType().equals(CameraConfigEntryType.TOGGLE) && newVal.getIntValue() == null) {
							value = 2;
						}
						valuePtr = new IntByReference(value).getPointer();
					break;
					case FLOAT:
						valuePtr = new FloatByReference(newVal.getFloatValue()).getPointer();
					break;
				}
				GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_set_value(pbrTargetWidget, valuePtr));
			}

			GP2ErrorHelper.checkResult(
					Gphoto2Library.INSTANCE.gp_camera_set_config(this.getCameraByReference(), pbrCameraConfigRoot, this.getContext().getPointerByRef()));
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		} finally {
			try {
				GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_free(pbrCameraConfigRoot));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	protected void visitWidgets(PointerByReference parentWidget, String parentPath, List<CameraConfigEntryBean> result) {
		final int widgetCountChildren = GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_count_children(parentWidget));

		final PointerByReference pbrWidgetName = new PointerByReference();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_name(parentWidget, pbrWidgetName));
		final String widgetName = pbrWidgetName.getValue().getString(0);

		final String widgetPath = parentPath + "/" + widgetName;

		final PointerByReference pbrWidgetLabel = new PointerByReference();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_label(parentWidget, pbrWidgetLabel));
		final String widgetLabel = pbrWidgetLabel.getValue().getString(0);

		final PointerByReference pbrWidgetInfo = new PointerByReference();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_info(parentWidget, pbrWidgetInfo));
		final String widgetInfo = pbrWidgetInfo.getValue().getString(0);

		final int widgetTypeCode;
		{
			final IntBuffer intBuff = IntBuffer.allocate(1);
			GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_type(parentWidget, intBuff));
			widgetTypeCode = intBuff.get();
		}
		final CameraConfigEntryType configEntryType = CameraConfigEntryType.getByCode(widgetTypeCode);

		final int widgetId;
		{
			final IntBuffer intBuff = IntBuffer.allocate(1);
			GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_id(parentWidget, intBuff));
			widgetId = intBuff.get();
		}

		Range range = null;
		String[] choices = null;

		if (configEntryType != null) {
			if (configEntryType.equals(CameraConfigEntryType.RANGE)) {
				final FloatBuffer fbMin = FloatBuffer.allocate(1);
				final FloatBuffer fbMax = FloatBuffer.allocate(1);
				final FloatBuffer fbStep = FloatBuffer.allocate(1);
				GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_range(parentWidget, fbMin, fbMax, fbStep));
				range = new Range(fbMin.get(), fbMax.get(), fbStep.get());
			}
			if (configEntryType.equals(CameraConfigEntryType.RADIO) || configEntryType.equals(CameraConfigEntryType.MENU)) {
				int choicesCount = GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_count_choices(parentWidget));
				choices = new String[choicesCount];
				for (int i = 0; i < choicesCount; i++) {
					PointerByReference pbrChoice = new PointerByReference();
					GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_choice(parentWidget, i, pbrChoice));
					choices[i] = pbrChoice.getValue().getString(0);
				}
			}
		}
		String strValue = null;
		Float floatValue = null;
		Integer intValue = null;
		if (configEntryType != null) {
			switch (configEntryType.getValueType()) {
				case STRING:
					PointerByReference byRefPointer = new PointerByReference();
					GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_value(parentWidget, byRefPointer.getPointer()));
					strValue = byRefPointer.getValue() != null ? byRefPointer.getValue().getString(0) : ""; // Not sure if null-check is necessary
				break;
				case FLOAT:
					FloatByReference byRefFloat = new FloatByReference();
					GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_value(parentWidget, byRefFloat.getPointer()));
					floatValue = byRefFloat.getValue();
				break;
				case INT:
					IntByReference byRefInteger = new IntByReference();
					GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_value(parentWidget, byRefInteger.getPointer()));
					intValue = byRefInteger.getValue();
				break;
				default:
			}
			result.add(
					new CameraConfigEntryBean(widgetId, widgetPath, widgetLabel, widgetInfo, configEntryType, intValue, floatValue, strValue, range, choices));
		}

		for (int i = 0; i < widgetCountChildren; i++) {
			final PointerByReference pbrChildWidget = new PointerByReference();
			GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_child(parentWidget, i, pbrChildWidget));
			pbrChildWidget.setPointer(pbrChildWidget.getValue());
			visitWidgets(pbrChildWidget, parentPath + "/" + widgetName, result);
		}
	}

	public List<CameraFileSystemEntryBean> listCameraFiles(final String path) {
		return listCameraFiles(path, true, false);
	}

	public List<CameraFileSystemEntryBean> listCameraFiles(final String path, final boolean recursive) {
		return listCameraFiles(path, true, recursive);
	}

	public List<CameraFileSystemEntryBean> listCameraFiles(final String path, final boolean includeFolders, final boolean recursive) {
		return listCameraFiles(path, true, includeFolders, recursive);
	}

	public List<CameraFileSystemEntryBean> listCameraFiles(final String path, final boolean inclueFiles, final boolean includeFolders,
			final boolean recursive) {
		final List<CameraFileSystemEntryBean> result;
		final List<String> folders;
		if (includeFolders || recursive) {
			folders = internalList(path, true);
		} else {
			folders = Collections.emptyList();
		}
		final List<String> files = inclueFiles ? internalList(path, false) : null;
		result = new ArrayList<CameraFileSystemEntryBean>((folders != null ? folders.size() : 0) + (files != null ? files.size() : 0));
		if (folders != null && (includeFolders || recursive)) {
			for (String folder : folders) {
				if (includeFolders) {
					result.add(new CameraFileSystemEntryBean(folder, path, true));
				}
				if (recursive) {
					result.addAll(listCameraFiles(path + folder + "/", inclueFiles, includeFolders, true));
				}
			}
		}
		if (files != null) {
			for (String file : files) {
				result.add(new CameraFileSystemEntryBean(file, path, false));
			}
		}
		return result;
	}

	protected List<String> internalList(final String path, boolean folders) {
		List<String> result;
		PointerByReference pbrListFiles = new PointerByReference();
		try {
			GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_list_new(pbrListFiles));
			pbrListFiles.setPointer(pbrListFiles.getValue());

			if (folders) {
				GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_folder_list_folders(this.getCameraByReference(), path, pbrListFiles,
						this.getContext().getPointerByRef()));
			} else {
				GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_folder_list_files(this.getCameraByReference(), path, pbrListFiles,
						this.getContext().getPointerByRef()));
			}

			int fileCount = GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_list_count(pbrListFiles));

			result = new ArrayList<String>(fileCount);
			for (int i = 0; i < fileCount; i++) {
				PointerByReference pbrFileName = new PointerByReference();
				GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_list_get_name(pbrListFiles, i, pbrFileName));
				final String fileName = pbrFileName.getValue().getString(0);
				result.add(fileName);
			}
		} finally {
			GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_list_unref(pbrListFiles));
		}
		return result;
	}

	public CameraFileImpl getFileInfo(final String path, final String fileName) {
		final PointerByReference pbrCameraFile = internalGetCameraFile(path, fileName, CameraFileType.GP_FILE_TYPE_NORMAL);
		CameraFileInfo.ByReference byRefCameraFileInfo;
		try {
			byRefCameraFileInfo = new CameraFileInfo.ByReference();
			GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_file_get_info(this.getCameraByReference(), path, fileName, byRefCameraFileInfo,
					this.getContext().getPointerByRef()));
		} finally {
			internalFreeCameraFileSafely(pbrCameraFile);
		}
		return new CameraFileImpl(byRefCameraFileInfo);
	}

	public byte[] getCameraFileContents(final String path, final String fileName) {
		return getCameraFileContents(path, fileName, false);
	}

	public byte[] getCameraFileContents(final String path, final String fileName, boolean preview) {
		final PointerByReference pbrCameraFile = internalGetCameraFile(path, fileName,
				preview ? CameraFileType.GP_FILE_TYPE_PREVIEW : CameraFileType.GP_FILE_TYPE_NORMAL);
		byte[] result;
		try {
			result = internalGetCameraFileData(pbrCameraFile);
		} finally {
			internalFreeCameraFileSafely(pbrCameraFile);
		}
		return result;
	}

	public void deleteCameraFile(final String path, final String fileName) {
		GP2ErrorHelper
				.checkResult(Gphoto2Library.INSTANCE.gp_camera_file_delete(this.getCameraByReference(), path, fileName, this.getContext().getPointerByRef()));
	}

	protected PointerByReference internalGetCameraFile(final String path, final String fileName, int type) {
		final PointerByReference pbrCameraFile = new PointerByReference();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_file_new(pbrCameraFile));
		pbrCameraFile.setPointer(pbrCameraFile.getValue());
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_camera_file_get(this.getCameraByReference(), path, fileName, type, pbrCameraFile,
				this.getContext().getPointerByRef()));

		return pbrCameraFile;
	}

	protected byte[] internalGetCameraFileData(PointerByReference cameraFile) {
		PointerByReference pref = new PointerByReference();
		LongByReference longByRef = new LongByReference();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_file_get_data_and_size(cameraFile, pref, longByRef));
		return pref.getValue().getByteArray(0, (int) longByRef.getValue());
	}

	protected void internalFreeCameraFileSafely(PointerByReference pbrFile) {
		Gphoto2Library.INSTANCE.gp_file_unref(pbrFile);
	}
}
