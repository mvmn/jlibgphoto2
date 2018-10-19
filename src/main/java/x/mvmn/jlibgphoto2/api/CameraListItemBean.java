package x.mvmn.jlibgphoto2.api;

public class CameraListItemBean {
	protected final String cameraModel;
	protected final String portName;

	public CameraListItemBean(String cameraModel, String portName) {
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
		CameraListItemBean other = (CameraListItemBean) obj;
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