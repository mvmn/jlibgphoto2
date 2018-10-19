package x.mvmn.jlibgphoto2.api.file;

public abstract class AbstractCameraFileImageInfoBean extends AbstractCameraFileInfoBean {

	protected final Integer width;
	protected final Integer height;

	public AbstractCameraFileImageInfoBean(final String mimeType, final Long size, final Boolean statusDownloaded, final Integer width, final Integer height) {
		super(mimeType, size, statusDownloaded);
		this.width = width;
		this.height = height;
	}

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AbstractCFImageInfoBean [width=").append(width).append(", height=").append(height).append(", mimeType=").append(mimeType)
				.append(", size=").append(size).append(", statusDownloaded=").append(statusDownloaded).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((height == null) ? 0 : height.hashCode());
		result = prime * result + ((width == null) ? 0 : width.hashCode());
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
		AbstractCameraFileImageInfoBean other = (AbstractCameraFileImageInfoBean) obj;
		if (height == null) {
			if (other.height != null)
				return false;
		} else if (!height.equals(other.height))
			return false;
		if (width == null) {
			if (other.width != null)
				return false;
		} else if (!width.equals(other.width))
			return false;
		return true;
	}
}