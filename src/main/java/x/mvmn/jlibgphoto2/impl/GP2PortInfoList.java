package x.mvmn.jlibgphoto2.impl;

import java.lang.reflect.Field;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sun.jna.ptr.PointerByReference;

import x.mvmn.gphoto2.jna.Gphoto2Library;
import x.mvmn.gphoto2.jna.Gphoto2Library.GPPortInfo;
import x.mvmn.gphoto2.jna.Gphoto2Library.GPPortType;
import x.mvmn.jlibgphoto2.util.GP2ErrorHelper;

public class GP2PortInfoList implements Iterable<GP2PortInfoList.GP2PortInfo> {

	public static void main(String args[]) {
		GP2PortInfoList list = new GP2PortInfoList();
		for (GP2PortInfo portInfo : list) {
			System.out.println(portInfo);
		}
		list.close();
	}

	protected static final Map<Integer, String> PORT_TYPE_NAMES;

	static {
		Map<Integer, String> portTypeNames = new HashMap<Integer, String>();
		for (Field field : GPPortType.class.getDeclaredFields()) {
			if (field.getName().startsWith("GP_PORT")) {
				try {
					portTypeNames.put(Integer.parseInt(field.get(null).toString()), field.getName());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		PORT_TYPE_NAMES = Collections.unmodifiableMap(portTypeNames);
	}

	protected final PointerByReference portInfoList;
	protected final Collection<GP2PortInfo> portInfoItems;
	protected final Map<String, GP2PortInfo> internalMapPathToGP2PortInfo = new HashMap<String, GP2PortInfo>();
	protected volatile boolean closed = false;

	public GP2PortInfoList() {
		final PointerByReference portInfoList = new PointerByReference();
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_port_info_list_new(portInfoList));
		portInfoList.setPointer(portInfoList.getValue());
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_port_info_list_load(portInfoList));

		List<GP2PortInfo> portInfoItems = new ArrayList<GP2PortInfo>();
		for (int i = 0; i < GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_port_info_list_count(portInfoList)); i++) {
			PointerByReference pbrGpPortInfo = new PointerByReference();
			Gphoto2Library.INSTANCE.gp_port_info_list_get_info(portInfoList, i, pbrGpPortInfo);
			GP2PortInfo portInfoItem = new GP2PortInfo(new GPPortInfo(pbrGpPortInfo.getValue()));
			portInfoItems.add(portInfoItem);
			internalMapPathToGP2PortInfo.put(portInfoItem.getPath(), portInfoItem);
		}

		this.portInfoList = portInfoList;
		this.portInfoItems = Collections.unmodifiableCollection(portInfoItems);
	}

	public GP2PortInfo getByPath(final String path) {
		return internalMapPathToGP2PortInfo.get(path);
	}

	public Iterator<GP2PortInfo> iterator() {
		checkClosed();
		return portInfoItems.iterator();
	}

	public int size() {
		return portInfoItems.size();
	}

	public void close() {
		GP2ErrorHelper.checkResult(Gphoto2Library.INSTANCE.gp_port_info_list_free(portInfoList));
		this.closed = true;
	}

	protected void checkClosed() {
		if (this.closed) {
			throw new RuntimeException("This GP2PortInfoList instance has already been closed.");
		}
	}
	// ====

	public class GP2PortInfo {

		protected final GPPortInfo gpPortInfo;

		GP2PortInfo(GPPortInfo gpPortInfo) {
			this.gpPortInfo = gpPortInfo;
		}

		GPPortInfo getGpPortInfo() {
			GP2PortInfoList.this.checkClosed();

			return gpPortInfo;
		}

		public String getName() {
			return getPortInfoStringField("name");
		}

		public String getPath() {
			return getPortInfoStringField("path");
		}

		public int getType() {
			GP2PortInfoList.this.checkClosed();

			IntBuffer ib = IntBuffer.allocate(1);
			Gphoto2Library.INSTANCE.gp_port_info_get_type(gpPortInfo, ib);
			return ib.get();
		}

		public String getTypeName() {
			int type = getType();
			String name = PORT_TYPE_NAMES.get(type);
			if (name == null) {
				name = String.valueOf(type);
			}
			return name;
		}

		private String getPortInfoStringField(final String fieldName) {
			GP2PortInfoList.this.checkClosed();

			PointerByReference pbr = new PointerByReference();
			try {
				Gphoto2Library.class.getMethod("gp_port_info_get_" + fieldName, GPPortInfo.class, PointerByReference.class).invoke(Gphoto2Library.INSTANCE,
						this.gpPortInfo, pbr);
			} catch (Exception e) {
				throw new RuntimeException("Error getting port info " + fieldName, e);
			}
			return pbr.getValue().getString(0);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("GP2PortInfo [name=").append(getName()).append(", path=").append(getPath()).append(", type=").append(getTypeName()).append("]");
			return builder.toString();
		}
	}
}