package x.mvmn.jlibgphoto2;

public class ConfigPropertyRangeBean {

	protected final Float min;
	protected final Float max;
	protected final Float step;

	public ConfigPropertyRangeBean(Float min, Float max, Float step) {
		this.min = min;
		this.max = max;
		this.step = step;
	}

	public Float getMin() {
		return min;
	}

	public Float getMax() {
		return max;
	}

	public Float getStep() {
		return step;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((max == null) ? 0 : max.hashCode());
		result = prime * result + ((min == null) ? 0 : min.hashCode());
		result = prime * result + ((step == null) ? 0 : step.hashCode());
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
		ConfigPropertyRangeBean other = (ConfigPropertyRangeBean) obj;
		if (max == null) {
			if (other.max != null)
				return false;
		} else if (!max.equals(other.max))
			return false;
		if (min == null) {
			if (other.min != null)
				return false;
		} else if (!min.equals(other.min))
			return false;
		if (step == null) {
			if (other.step != null)
				return false;
		} else if (!step.equals(other.step))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConfigPropertyRangeBean [min=").append(min).append(", max=").append(max).append(", step=").append(step).append("]");
		return builder.toString();
	}
}
