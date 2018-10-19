package x.mvmn.jlibgphoto2.api.file;

public abstract class AbstractCameraFileInfoBean {

	protected final String mimeType;
	protected final Long size;
	protected final Boolean statusDownloaded;

	public AbstractCameraFileInfoBean(final String mimeType, final Long size, final Boolean statusDownloaded) {
		this.mimeType = mimeType;
		this.size = size;
		this.statusDownloaded = statusDownloaded;
	}

	public String getMimeType() {
		return mimeType;
	}

	public Long getSize() {
		return size;
	}

	public Boolean getStatusDownloaded() {
		return statusDownloaded;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AbstractCFInfoBean [mimeType=").append(mimeType).append(", size=").append(size).append(", statusDownloaded=").append(statusDownloaded)
				.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mimeType == null) ? 0 : mimeType.hashCode());
		result = prime * result + ((size == null) ? 0 : size.hashCode());
		result = prime * result + ((statusDownloaded == null) ? 0 : statusDownloaded.hashCode());
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
		AbstractCameraFileInfoBean other = (AbstractCameraFileInfoBean) obj;
		if (mimeType == null) {
			if (other.mimeType != null)
				return false;
		} else if (!mimeType.equals(other.mimeType))
			return false;
		if (size == null) {
			if (other.size != null)
				return false;
		} else if (!size.equals(other.size))
			return false;
		if (statusDownloaded == null) {
			if (other.statusDownloaded != null)
				return false;
		} else if (!statusDownloaded.equals(other.statusDownloaded))
			return false;
		return true;
	}
}