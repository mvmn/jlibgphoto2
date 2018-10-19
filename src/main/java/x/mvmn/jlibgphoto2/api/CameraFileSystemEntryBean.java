package x.mvmn.jlibgphoto2.api;

public class CameraFileSystemEntryBean implements Comparable<CameraFileSystemEntryBean> {

	protected final String name;
	protected final String path;
	protected final boolean folder;

	public CameraFileSystemEntryBean(final String name, final String path, final boolean folder) {
		this.name = name;
		this.path = path;
		this.folder = folder;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public boolean isFolder() {
		return folder;
	}

	public boolean isFile() {
		return !folder;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (folder ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
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
		CameraFileSystemEntryBean other = (CameraFileSystemEntryBean) obj;
		if (folder != other.folder)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CameraFileSystemEntryBean [name=").append(name).append(", path=").append(path).append(", folder=").append(folder).append("]");
		return builder.toString();
	}

	public int compareTo(CameraFileSystemEntryBean other) {
		String fp = (this.getPath() != null ? this.getPath() : "") + "/" + (this.getName() != null ? this.getName() : "");
		String fpOther = (other.getPath() != null ? other.getPath() : "") + "/" + (other.getName() != null ? other.getName() : "");
		return fp.compareTo(fpOther);
	}
}
