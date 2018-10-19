package x.mvmn.jlibgphoto2.impl;

import com.sun.jna.Native;

import x.mvmn.gphoto2.jna.CameraFileInfo;
import x.mvmn.gphoto2.jna.Gphoto2Library.CameraFileInfoFields;
import x.mvmn.gphoto2.jna.Gphoto2Library.CameraFileStatus;
import x.mvmn.jlibgphoto2.api.file.CameraFile;
import x.mvmn.jlibgphoto2.api.file.CameraFileAudioInfoBean;
import x.mvmn.jlibgphoto2.api.file.CameraFileImageInfoBean;
import x.mvmn.jlibgphoto2.api.file.CameraFilePreviewInfoBean;

public class CameraFileImpl implements CameraFile {

	protected final CameraFileAudioInfoBean audio;
	protected final CameraFileImageInfoBean file;
	protected final CameraFilePreviewInfoBean preview;

	CameraFileImpl(final CameraFileInfo cameraFileInfo) {
		audio = cameraFileInfo.audio != null
				? new CameraFileAudioInfoBean(getType(cameraFileInfo.audio.fields, cameraFileInfo.audio.type),
						getSize(cameraFileInfo.audio.fields, cameraFileInfo.audio.size), getStatus(cameraFileInfo.audio.fields, cameraFileInfo.audio.status))
				: null;
		file = cameraFileInfo.file != null
				? new CameraFileImageInfoBean(getType(cameraFileInfo.file.fields, cameraFileInfo.file.type),
						getSize(cameraFileInfo.file.fields, cameraFileInfo.file.size), getStatus(cameraFileInfo.file.fields, cameraFileInfo.file.status),
						getWidth(cameraFileInfo.file.fields, cameraFileInfo.file.width), getHeight(cameraFileInfo.file.fields, cameraFileInfo.file.height),
						getPermissions(cameraFileInfo.file.fields, cameraFileInfo.file.permissions))
				: null;
		preview = cameraFileInfo.preview != null ? new CameraFilePreviewInfoBean(getType(cameraFileInfo.preview.fields, cameraFileInfo.preview.type),
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
		return containsField(fields, CameraFileInfoFields.GP_FILE_INFO_TYPE) ? Native.toString(type, GP2CameraImpl.NATIVE_STRING_ENCODING) : null;
	}

	protected boolean containsField(int fields, int field) {
		return (fields & field) != 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.jlibgphoto2.impl.CameraFileInfo#getAudio()
	 */
	public CameraFileAudioInfoBean getAudioInfo() {
		return audio;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.jlibgphoto2.impl.CameraFileInfo#getFile()
	 */
	public CameraFileImageInfoBean getImageInfo() {
		return file;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see x.mvmn.jlibgphoto2.impl.CameraFileInfo#getPreview()
	 */
	public CameraFilePreviewInfoBean getPreviewInfo() {
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
		CameraFileImpl other = (CameraFileImpl) obj;
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
