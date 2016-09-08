package x.mvmn.jlibgphoto2;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import x.mvmn.gphoto2.jna.Gphoto2Library;
import x.mvmn.jlibgphoto2.CameraConfigEntryBean.CameraConfigEntryType;
import x.mvmn.jlibgphoto2.CameraConfigEntryBean.Range;
import x.mvmn.jlibgphoto2.util.GP2ErrorHelper;

public class GP2ConfigHelper {

	public static void main(String args[]) throws Exception {
		GP2Camera camera = new GP2Camera();
		CameraConfigEntryBean viewfinder = null;
		CameraConfigEntryBean focusdrive = null;
		for (CameraConfigEntryBean configEntry : getConfig(camera)) {
			System.out.println(configEntry);
			if (configEntry.getPath().equals("/main/actions/viewfinder")) {
				viewfinder = configEntry;
			} else if (configEntry.getPath().equals("/main/actions/manualfocusdrive")) {
				focusdrive = configEntry;
			}
		}
		setConfig(camera, viewfinder.cloneWithNewValue(1));
		final String[] focusChoices = focusdrive.getChoices();
		for (int i = 0; i < 10; i++) {
			setConfig(camera, focusdrive.cloneWithNewValue(focusChoices[1]));
			Thread.sleep(100);
		}
		for (int i = 0; i < 10; i++) {
			setConfig(camera, focusdrive.cloneWithNewValue(focusChoices[(int) Math.ceil(focusChoices.length / 2d) + 1]));
			Thread.sleep(100);
		}
		camera.close();
	}

	public static void setConfig(GP2Camera camera, CameraConfigEntryBean... newValues) {
		PointerByReference pbrCameraConfigRoot = new PointerByReference();
		try {
			GP2ErrorHelper.checkResult(
					Gphoto2Library.INSTANCE.gp_camera_get_config(camera.getCameraByReference(), pbrCameraConfigRoot, camera.getContext().getPointerByRef()));
			pbrCameraConfigRoot.setPointer(pbrCameraConfigRoot.getValue());
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
					Gphoto2Library.INSTANCE.gp_camera_set_config(camera.getCameraByReference(), pbrCameraConfigRoot, camera.getContext().getPointerByRef()));
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

	public static List<CameraConfigEntryBean> getConfig(GP2Camera camera) {
		List<CameraConfigEntryBean> result = new ArrayList<CameraConfigEntryBean>();
		PointerByReference pbrCameraConfigRoot = new PointerByReference();
		try {
			GP2ErrorHelper.checkResult(
					Gphoto2Library.INSTANCE.gp_camera_get_config(camera.getCameraByReference(), pbrCameraConfigRoot, camera.getContext().getPointerByRef()));
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

	protected static void visitWidgets(PointerByReference parentWidget, String parentPath, List<CameraConfigEntryBean> result) {
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
					GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_value(parentWidget, byRefPointer));
					strValue = byRefPointer.getValue() != null ? byRefPointer.getValue().getString(0) : ""; // Not sure if null-check is necessary
				break;
				case FLOAT:
					FloatByReference byRefFloat = new FloatByReference();
					GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_value(parentWidget, byRefFloat));
					floatValue = byRefFloat.getValue();
				break;
				case INT:
					IntByReference byRefInteger = new IntByReference();
					GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_value(parentWidget, byRefInteger));
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
}
