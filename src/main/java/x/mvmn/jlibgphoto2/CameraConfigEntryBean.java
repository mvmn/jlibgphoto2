package x.mvmn.jlibgphoto2;

import java.util.Arrays;

import x.mvmn.gphoto2.jna.Gphoto2Library.CameraWidgetType;

public class CameraConfigEntryBean {

	public static enum CameraConfigEntryValueType {
		STRING, INT, FLOAT;
	}

	public static enum CameraConfigEntryType {
		TEXT(CameraWidgetType.GP_WIDGET_TEXT, CameraConfigEntryValueType.STRING), RANGE(CameraWidgetType.GP_WIDGET_RANGE,
				CameraConfigEntryValueType.FLOAT), TOGGLE(CameraWidgetType.GP_WIDGET_TOGGLE, CameraConfigEntryValueType.INT), RADIO(
						CameraWidgetType.GP_WIDGET_RADIO, CameraConfigEntryValueType.STRING), MENU(CameraWidgetType.GP_WIDGET_MENU,
								CameraConfigEntryValueType.STRING), DATE(CameraWidgetType.GP_WIDGET_DATE, CameraConfigEntryValueType.INT);

		private final int code;
		private final CameraConfigEntryValueType valueType;

		private CameraConfigEntryType(final int code, final CameraConfigEntryValueType valueType) {
			this.code = code;
			this.valueType = valueType;
		}

		public int getCode() {
			return code;
		}

		public CameraConfigEntryValueType getValueType() {
			return valueType;
		}

		public static CameraConfigEntryType getByCode(int code) {
			CameraConfigEntryType result = null;
			for (CameraConfigEntryType type : CameraConfigEntryType.values()) {
				if (type.getCode() == code) {
					result = type;
					break;
				}
			}
			return result;
		}
	}

	public static class Range {
		protected final float min;
		protected final float max;
		protected final float step;

		public Range(float min, float max, float step) {
			super();
			this.min = min;
			this.max = max;
			this.step = step;
		}

		public float getMin() {
			return min;
		}

		public float getMax() {
			return max;
		}

		public float getStep() {
			return step;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Range [min=").append(min).append(", max=").append(max).append(", step=").append(step).append("]");
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Float.floatToIntBits(max);
			result = prime * result + Float.floatToIntBits(min);
			result = prime * result + Float.floatToIntBits(step);
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
			CameraConfigEntryBean.Range other = (CameraConfigEntryBean.Range) obj;
			if (Float.floatToIntBits(max) != Float.floatToIntBits(other.max))
				return false;
			if (Float.floatToIntBits(min) != Float.floatToIntBits(other.min))
				return false;
			if (Float.floatToIntBits(step) != Float.floatToIntBits(other.step))
				return false;
			return true;
		}
	}

	protected final int id;
	protected final String path;
	protected final String label;
	protected final String info;
	protected final CameraConfigEntryType type;
	protected final Integer intValue;
	protected final Float floatValue;
	protected final String strValue;
	protected final CameraConfigEntryBean.Range range;
	protected final String[] choices;

	public CameraConfigEntryBean(int id, String path, String label, String info, CameraConfigEntryType type, Integer intValue, Float floatValue,
			String strValue, CameraConfigEntryBean.Range range, String[] choices) {
		super();
		this.id = id;
		this.path = path;
		this.label = label;
		this.info = info;
		this.type = type;
		this.intValue = intValue;
		this.floatValue = floatValue;
		this.strValue = strValue;
		this.range = range;
		this.choices = choices;
	}

	public CameraConfigEntryBean cloneWithNewValue(String strValue) {
		if (this.getType().getValueType() != CameraConfigEntryValueType.STRING) {
			throw new RuntimeException("Setting STRING value instead of expected " + this.getType().getValueType());
		}
		return new CameraConfigEntryBean(id, path, label, info, type, intValue, floatValue, strValue, range, choices);
	}

	public CameraConfigEntryBean cloneWithNewValue(int intValue) {
		if (this.getType().getValueType() != CameraConfigEntryValueType.INT) {
			throw new RuntimeException("Setting INT value instead of expected " + this.getType().getValueType());
		}
		return new CameraConfigEntryBean(id, path, label, info, type, intValue, floatValue, strValue, range, choices);
	}

	public CameraConfigEntryBean cloneWithNewValue(float floatValue) {
		if (this.getType().getValueType() != CameraConfigEntryValueType.FLOAT) {
			throw new RuntimeException("Setting FLOAT value instead of expected " + this.getType().getValueType());
		}
		return new CameraConfigEntryBean(id, path, label, info, type, intValue, floatValue, strValue, range, choices);
	}

	public int getId() {
		return id;
	}

	public String getPath() {
		return path;
	}

	public String getLabel() {
		return label;
	}

	public String getInfo() {
		return info;
	}

	public CameraConfigEntryType getType() {
		return type;
	}

	public Integer getIntValue() {
		return intValue;
	}

	public Float getFloatValue() {
		return floatValue;
	}

	public String getStrValue() {
		return strValue;
	}

	public Object getValue() {
		Object result = null;
		switch (type.valueType) {
			case STRING:
				result = getStrValue();
			break;
			case FLOAT:
				result = getFloatValue();
			break;
			case INT:
				result = getIntValue();
			break;
		}
		return result;
	}

	public CameraConfigEntryBean.Range getRange() {
		return range;
	}

	public String[] getChoices() {
		return choices;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(choices);
		result = prime * result + ((floatValue == null) ? 0 : floatValue.hashCode());
		result = prime * result + id;
		result = prime * result + ((info == null) ? 0 : info.hashCode());
		result = prime * result + ((intValue == null) ? 0 : intValue.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((range == null) ? 0 : range.hashCode());
		result = prime * result + ((strValue == null) ? 0 : strValue.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		CameraConfigEntryBean other = (CameraConfigEntryBean) obj;
		if (!Arrays.equals(choices, other.choices))
			return false;
		if (floatValue == null) {
			if (other.floatValue != null)
				return false;
		} else if (!floatValue.equals(other.floatValue))
			return false;
		if (id != other.id)
			return false;
		if (info == null) {
			if (other.info != null)
				return false;
		} else if (!info.equals(other.info))
			return false;
		if (intValue == null) {
			if (other.intValue != null)
				return false;
		} else if (!intValue.equals(other.intValue))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (range == null) {
			if (other.range != null)
				return false;
		} else if (!range.equals(other.range))
			return false;
		if (strValue == null) {
			if (other.strValue != null)
				return false;
		} else if (!strValue.equals(other.strValue))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CameraConfigEntryBean [id=").append(id).append(", path=").append(path).append(", label=").append(label).append(", info=").append(info)
				.append(", type=").append(type).append(", intValue=").append(intValue).append(", floatValue=").append(floatValue).append(", strValue=")
				.append(strValue).append(", range=").append(range).append(", choices=").append(Arrays.toString(choices)).append("]");
		return builder.toString();
	}

}