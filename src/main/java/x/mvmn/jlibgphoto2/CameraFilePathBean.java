package x.mvmn.jlibgphoto2;

import com.sun.jna.Native;

import x.mvmn.gphoto2.jna.CameraFilePath;

public class CameraFilePathBean {

	protected static final String encpding = System.getProperty("jlibgphoto2.camerafilepath.encoding", "ASCII");

	protected final String fileName;
	protected final String folderName;

	CameraFilePathBean(CameraFilePath cameraFilePath) {
		this(Native.toString(cameraFilePath.name, "ASCII"), Native.toString(cameraFilePath.folder, "ASCII"));
	}

	public CameraFilePathBean(String fileName, String folderName) {
		super();
		this.fileName = fileName;
		this.folderName = folderName;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFolderName() {
		return folderName;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GP2CameraFilePath [fileName=").append(fileName).append(", folderName=").append(folderName).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((folderName == null) ? 0 : folderName.hashCode());
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
		CameraFilePathBean other = (CameraFilePathBean) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (folderName == null) {
			if (other.folderName != null)
				return false;
		} else if (!folderName.equals(other.folderName))
			return false;
		return true;
	}
}
