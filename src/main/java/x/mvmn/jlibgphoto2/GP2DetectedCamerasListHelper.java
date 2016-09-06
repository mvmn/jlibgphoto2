package x.mvmn.jlibgphoto2;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.sun.jna.ptr.PointerByReference;

import x.mvmn.gphoto2.jna.Gphoto2Library;
import x.mvmn.jlibgphoto2.util.GP2ErrorHelper;

public class GP2DetectedCamerasListHelper {

	public static class CameraListItem {
		protected final String cameraModel;
		protected final String portName;

		public CameraListItem(String cameraModel, String portName) {
			super();
			this.cameraModel = cameraModel;
			this.portName = portName;
		}

		public String getCameraModel() {
			return cameraModel;
		}

		public String getPortName() {
			return portName;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("CameraListItem [cameraModel=").append(cameraModel).append(", portName=").append(portName).append("]");
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((cameraModel == null) ? 0 : cameraModel.hashCode());
			result = prime * result + ((portName == null) ? 0 : portName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CameraListItem other = (CameraListItem) obj;
			if (cameraModel == null) {
				if (other.cameraModel != null)
					return false;
			} else if (!cameraModel.equals(other.cameraModel))
				return false;
			if (portName == null) {
				if (other.portName != null)
					return false;
			} else if (!portName.equals(other.portName))
				return false;
			return true;
		}
	}

	public static void main(String args[]) {
		GP2Context context = new GP2Context();
		for (CameraListItem cameraListItem : autodetectCameras(context)) {
			System.out.println(cameraListItem);
		}
	}

	public static List<CameraListItem> autodetectCameras(GP2Context gp2Context) {
		final List<CameraListItem> result = new ArrayList<CameraListItem>();

		processPortList(gp2Context, new Consumer<CameraListItem>() {
			public void accept(CameraListItem item) {
				result.add(item);
			}
		});

		return result;
	}

	protected static void processPortList(GP2Context gp2Context, Consumer<CameraListItem> consumer) {
		PointerByReference context = gp2Context.getPointer();

		final PointerByReference tempList = new PointerByReference();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_list_new(tempList));
		tempList.setPointer(tempList.getValue());

		final PointerByReference portInfoList = new PointerByReference();
		try {
			final PointerByReference cameraAbilitiesList = new PointerByReference();
			GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_abilities_list_new(cameraAbilitiesList));
			cameraAbilitiesList.setPointer(cameraAbilitiesList.getValue());
			try {
				GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_port_info_list_new(portInfoList));
				portInfoList.setPointer(portInfoList.getValue());
				try {
					GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_port_info_list_load(portInfoList));
					GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_abilities_list_load(cameraAbilitiesList, context));
					GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_abilities_list_detect(cameraAbilitiesList, portInfoList, tempList, context));
					final int count = GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_list_count(tempList));
					for (int i = 0; i < count; i++) {
						final PointerByReference pmodel = new PointerByReference();
						GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_list_get_name(tempList, i, pmodel));
						final String model = pmodel.getValue().getString(0);
						final PointerByReference pvalue = new PointerByReference();
						GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_list_get_value(tempList, i, pvalue));
						final String path = pvalue.getValue().getString(0);
						if (path.startsWith("usb:")) { // Why only USB? IDK, but that's what gp_camera_autodetect does
							// https://github.com/gphoto/libgphoto2/blob/master/libgphoto2/gphoto2-camera.c#L654
							CameraListItem item = new CameraListItem(model, path);
							if (consumer != null) {
								consumer.accept(item);
							}
						}
					}
				} finally {
					Gphoto2Library.INSTANCE.gp_port_info_list_free(portInfoList);
				}
			} finally {
				Gphoto2Library.INSTANCE.gp_abilities_list_free(cameraAbilitiesList);
			}
		} finally {
			Gphoto2Library.INSTANCE.gp_list_free(tempList);
		}
	}
}
