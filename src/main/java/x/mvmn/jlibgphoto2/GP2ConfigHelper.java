package x.mvmn.jlibgphoto2;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import x.mvmn.gphoto2.jna.Gphoto2Library;
import x.mvmn.gphoto2.jna.Gphoto2Library.CameraWidgetType;
import x.mvmn.jlibgphoto2.util.GP2ErrorHelper;

public class GP2ConfigHelper {

	public static void main(String args[]) throws Exception {
		GP2Camera camera = new GP2Camera();
		getConfig(camera);
		camera.close();
	}

	protected static enum GP2CameraWidgetType {
		GP_WIDGET_WINDOW(CameraWidgetType.GP_WIDGET_WINDOW), GP_WIDGET_SECTION(CameraWidgetType.GP_WIDGET_SECTION), GP_WIDGET_TEXT(
				CameraWidgetType.GP_WIDGET_TEXT), GP_WIDGET_RANGE(CameraWidgetType.GP_WIDGET_RANGE), GP_WIDGET_TOGGLE(
						CameraWidgetType.GP_WIDGET_TOGGLE), GP_WIDGET_RADIO(CameraWidgetType.GP_WIDGET_RADIO), GP_WIDGET_MENU(
								CameraWidgetType.GP_WIDGET_MENU), GP_WIDGET_BUTTON(
										CameraWidgetType.GP_WIDGET_BUTTON), GP_WIDGET_DATE(CameraWidgetType.GP_WIDGET_DATE);

		private final int code;

		private GP2CameraWidgetType(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static GP2CameraWidgetType getByCode(int code) {
			GP2CameraWidgetType result = null;
			for (GP2CameraWidgetType type : GP2CameraWidgetType.values()) {
				if (type.getCode() == code) {
					result = type;
					break;
				}
			}
			return result;
		}
	}

	public static void getConfig(GP2Camera camera) {
		PointerByReference cameraWidget = new PointerByReference();
		try {
			GP2ErrorHelper.checkResult(
					Gphoto2Library.INSTANCE.gp_camera_get_config(camera.getCameraByReference(), cameraWidget, camera.getContext().getPointerByRef()));
			cameraWidget.setPointer(cameraWidget.getValue());
			visitWidgets(cameraWidget, "");
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		} finally {
			try {
				GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_free(cameraWidget));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	protected static void visitWidgets(PointerByReference parentWidget, String parentPath) {
		final int widgetCountChildren = GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_count_children(parentWidget));

		final PointerByReference pbrWidgetName = new PointerByReference();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_name(parentWidget, pbrWidgetName));
		final String widgetName = pbrWidgetName.getValue().getString(0);

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
		final GP2CameraWidgetType widgetType = GP2CameraWidgetType.getByCode(widgetTypeCode);

		final int widgetId;
		{
			final IntBuffer intBuff = IntBuffer.allocate(1);
			GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_id(parentWidget, intBuff));
			widgetId = intBuff.get();
		}

		System.out.println(widgetId + " - " + widgetLabel + " (" + widgetInfo + "): " + parentPath + "/" + widgetName + " " + widgetType);

		if (widgetType.equals(GP2CameraWidgetType.GP_WIDGET_RANGE)) {
			final FloatBuffer fbMin = FloatBuffer.allocate(1);
			final FloatBuffer fbMax = FloatBuffer.allocate(1);
			final FloatBuffer fbStep = FloatBuffer.allocate(1);
			GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_range(parentWidget, fbMin, fbMax, fbStep));
			ConfigPropertyRangeBean range = new ConfigPropertyRangeBean(fbMin.get(), fbMax.get(), fbStep.get());
			System.out.println(" - Range: " + range);
		}
		if (widgetType.equals(GP2CameraWidgetType.GP_WIDGET_RADIO) || widgetType.equals(GP2CameraWidgetType.GP_WIDGET_MENU)) {
			int choicesCount = GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_count_choices(parentWidget));
			for (int i = 0; i < choicesCount; i++) {
				PointerByReference pbrChoice = new PointerByReference();
				GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_choice(parentWidget, i, pbrChoice));
				System.out.println(" - Choice: " + pbrChoice.getValue().getString(0));
			}
		}
		if (!widgetType.equals(GP2CameraWidgetType.GP_WIDGET_WINDOW) && !widgetType.equals(GP2CameraWidgetType.GP_WIDGET_SECTION)
				&& !widgetType.equals(GP2CameraWidgetType.GP_WIDGET_BUTTON)) {
			switch (widgetType) {
				case GP_WIDGET_MENU:
				case GP_WIDGET_RADIO:
				case GP_WIDGET_TEXT:
					PointerByReference pbr = new PointerByReference();
					GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_value(parentWidget, pbr));
					System.out.println("! Value == " + (pbr.getValue() != null ? pbr.getValue().getString(0) : null));
				break;
				case GP_WIDGET_RANGE:
					FloatByReference fbr = new FloatByReference();
					GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_value(parentWidget, fbr));
					System.out.println("! Value == " + fbr.getValue());
				break;
				case GP_WIDGET_TOGGLE:
				case GP_WIDGET_DATE:
					IntByReference ibr = new IntByReference();
					GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_value(parentWidget, ibr));
					System.out.println("! Value == " + ibr.getValue());
				break;
				default:
			}
		}

		for (int i = 0; i < widgetCountChildren; i++) {
			final PointerByReference pbrChildWidget = new PointerByReference();
			GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_widget_get_child(parentWidget, i, pbrChildWidget));
			pbrChildWidget.setPointer(pbrChildWidget.getValue());
			visitWidgets(pbrChildWidget, parentPath + "/" + widgetName);
		}
	}
}
