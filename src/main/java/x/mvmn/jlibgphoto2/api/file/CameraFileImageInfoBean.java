package x.mvmn.jlibgphoto2.api.file;

public class CameraFileImageInfoBean extends AbstractCameraFileImageInfoBean {
	protected final Integer permissions;

	public CameraFileImageInfoBean(String mimeType, Long size, Boolean statusDownloaded, Integer width, Integer height, Integer permissions) {
		super(mimeType, size, statusDownloaded, width, height);
		this.permissions = permissions;
	}

	public Integer getPermissions() {
		return permissions;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CFFileInfoBean [permissions=").append(permissions).append(", width=").append(width).append(", height=").append(height)
				.append(", mimeType=").append(mimeType).append(", size=").append(size).append(", statusDownloaded=").append(statusDownloaded).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((permissions == null) ? 0 : permissions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CameraFileImageInfoBean other = (CameraFileImageInfoBean) obj;
		if (permissions == null) {
			if (other.permissions != null)
				return false;
		} else if (!permissions.equals(other.permissions))
			return false;
		return true;
	}
}