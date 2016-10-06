package x.mvmn.jlibgphoto2;

import com.sun.jna.Native;

import x.mvmn.gphoto2.jna.CameraFileInfo;
import x.mvmn.gphoto2.jna.Gphoto2Library.CameraFileInfoFields;
import x.mvmn.gphoto2.jna.Gphoto2Library.CameraFileStatus;

public class CameraFileInfoBean {
	protected static final String NATIVE_STRING_ENCODING = System.getProperty("jlibgphoto2.camerafilepath.encoding", "ASCII");

	public abstract class AbstractCFInfoBean {

		protected final String mimeType;
		protected final Long size;
		protected final Boolean statusDownloaded;

		public AbstractCFInfoBean(final String mimeType, final Long size, final Boolean statusDownloaded) {
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
			builder.append("AbstractCFInfoBean [mimeType=").append(mimeType).append(", size=").append(size).append(", statusDownloaded=")
					.append(statusDownloaded).append("]");
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
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
			AbstractCFInfoBean other = (AbstractCFInfoBean) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
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

		private CameraFileInfoBean getOuterType() {
			return CameraFileInfoBean.this;
		}
	}

	public abstract class AbstractCFImageInfoBean extends AbstractCFInfoBean {

		protected final Integer width;
		protected final Integer height;

		public AbstractCFImageInfoBean(final String mimeType, final Long size, final Boolean statusDownloaded, final Integer width, final Integer height) {
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
			result = prime * result + getOuterType().hashCode();
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
			AbstractCFImageInfoBean other = (AbstractCFImageInfoBean) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
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

		private CameraFileInfoBean getOuterType() {
			return CameraFileInfoBean.this;
		}
	}

	public class CFAudioInfoBean extends AbstractCFInfoBean {
		public CFAudioInfoBean(final String mimeType, final Long size, final Boolean statusDownloaded) {
			super(mimeType, size, statusDownloaded);
		}
	}

	public class CFPreviewInfoBean extends AbstractCFImageInfoBean {
		public CFPreviewInfoBean(String mimeType, Long size, Boolean statusDownloaded, Integer width, Integer height) {
			super(mimeType, size, statusDownloaded, width, height);
		}
	}

	public class CFFileInfoBean extends AbstractCFImageInfoBean {
		protected final Integer permissions;

		public CFFileInfoBean(String mimeType, Long size, Boolean statusDownloaded, Integer width, Integer height, Integer permissions) {
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
			result = prime * result + getOuterType().hashCode();
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
			CFFileInfoBean other = (CFFileInfoBean) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (permissions == null) {
				if (other.permissions != null)
					return false;
			} else if (!permissions.equals(other.permissions))
				return false;
			return true;
		}

		private CameraFileInfoBean getOuterType() {
			return CameraFileInfoBean.this;
		}

	}

	protected final CFAudioInfoBean audio;
	protected final CFFileInfoBean file;
	protected final CFPreviewInfoBean preview;

	CameraFileInfoBean(final CameraFileInfo cameraFileInfo) {
		audio = cameraFileInfo.audio != null
				? new CFAudioInfoBean(getType(cameraFileInfo.audio.fields, cameraFileInfo.audio.type),
						getSize(cameraFileInfo.audio.fields, cameraFileInfo.audio.size), getStatus(cameraFileInfo.audio.fields, cameraFileInfo.audio.status))
				: null;
		file = cameraFileInfo.file != null ? new CFFileInfoBean(getType(cameraFileInfo.file.fields, cameraFileInfo.file.type),
				getSize(cameraFileInfo.file.fields, cameraFileInfo.file.size), getStatus(cameraFileInfo.file.fields, cameraFileInfo.file.status),
				getWidth(cameraFileInfo.file.fields, cameraFileInfo.file.width), getHeight(cameraFileInfo.file.fields, cameraFileInfo.file.height),
				getPermissions(cameraFileInfo.file.fields, cameraFileInfo.file.permissions)) : null;
		preview = cameraFileInfo.preview != null ? new CFPreviewInfoBean(getType(cameraFileInfo.preview.fields, cameraFileInfo.preview.type),
				getSize(cameraFileInfo.preview.fields, cameraFileInfo.preview.size), getStatus(cameraFileInfo.preview.fields, cameraFileInfo.preview.status),
				getWidth(cameraFileInfo.preview.fields, cameraFileInfo.preview.width), getHeight(cameraFileInfo.preview.fields, cameraFileInfo.preview.height))
				: null;
	}

	protected Integer getPermissions(int fields, int permissions) {
		return containsField(fields, CameraFileInfoFields.GP_FILE_INFO_PERMISSIONS) ? Integer.valueOf(permissions) : null;
	}

	protected Integer getHeight(int fields, int height) {
		return containsField(fields, CameraFileInfoFields.GP_FILE_INFO_HEIGHT) ? Integer.valueOf(height) : null;
	}

	protected Integer getWidth(int fields, int width) {
		return containsField(fields, CameraFileInfoFields.GP_FILE_INFO_WIDTH) ? Integer.valueOf(width) : null;
	}

	protected Boolean getStatus(int fields, int status) {
		return containsField(fields, CameraFileInfoFields.GP_FILE_INFO_STATUS) ? Boolean.valueOf(status == CameraFileStatus.GP_FILE_STATUS_DOWNLOADED) : null;
	}

	protected Long getSize(int fields, long size) {
		return containsField(fields, CameraFileInfoFields.GP_FILE_INFO_SIZE) ? Long.valueOf(size) : null;
	}

	protected String getType(int fields, byte[] type) {
		return containsField(fields, CameraFileInfoFields.GP_FILE_INFO_TYPE) ? Native.toString(type, NATIVE_STRING_ENCODING) : null;
	}

	protected boolean containsField(int fields, int field) {
		return (fields & field) != 0;
	}

	public CFAudioInfoBean getAudio() {
		return audio;
	}

	public CFFileInfoBean getFile() {
		return file;
	}

	public CFPreviewInfoBean getPreview() {
		return preview;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CameraFileInfoBean [audio=").append(audio).append(", file=").append(file).append(", preview=").append(preview).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((audio == null) ? 0 : audio.hashCode());
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((preview == null) ? 0 : preview.hashCode());
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
		CameraFileInfoBean other = (CameraFileInfoBean) obj;
		if (audio == null) {
			if (other.audio != null)
				return false;
		} else if (!audio.equals(other.audio))
			return false;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (preview == null) {
			if (other.preview != null)
				return false;
		} else if (!preview.equals(other.preview))
			return false;
		return true;
	}
}
