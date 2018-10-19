package x.mvmn.jlibgphoto2.api.file;

public class CameraFilePreviewInfoBean extends AbstractCameraFileImageInfoBean {
	public CameraFilePreviewInfoBean(String mimeType, Long size, Boolean statusDownloaded, Integer width, Integer height) {
		super(mimeType, size, statusDownloaded, width, height);
	}
}