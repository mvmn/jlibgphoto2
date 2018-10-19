package x.mvmn.jlibgphoto2.impl;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.ptr.PointerByReference;

import x.mvmn.gphoto2.jna.Gphoto2Library;
import x.mvmn.jlibgphoto2.api.CameraDetector;
import x.mvmn.jlibgphoto2.api.CameraListItemBean;
import x.mvmn.jlibgphoto2.util.GP2ErrorHelper;

public class CameraDetectorImpl implements CameraDetector {

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.jlibgphoto2.impl.CameraDetector#detectCameras()
	 */
	public List<CameraListItemBean> detectCameras() {
		return processPortList(new GP2Context());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.jlibgphoto2.impl.CameraDetector#detectCameras(x.mvmn.jlibgphoto2.impl.GP2Context)
	 */
	public List<CameraListItemBean> detectCameras(GP2Context gp2Context) {
		return processPortList(gp2Context != null ? gp2Context : new GP2Context());
	}

	protected static List<CameraListItemBean> processPortList(GP2Context gp2Context) {
		List<CameraListItemBean> resultList = new ArrayList<CameraListItemBean>();
		PointerByReference context = gp2Context.getPointerByRef();

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
							resultList.add(new CameraListItemBean(model, path));
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
		return resultList;
	}
}
