package flowsolver;

import java.io.Serializable;

public class FlowRequest implements Serializable{

	private static final long serialVersionUID = -5341413177306223467L;
	private Domain src;
	private Domain dst;
	private TrafficClass tClass;
	private int priority;
	private FlowAction fAction;
	
	//***CONSTRUCTORS	
	public FlowRequest(Domain src, Domain dst, TrafficClass tClass,int priority, FlowAction fAction) {
		this.src = src;
		this.dst = dst;
		this.tClass = tClass;
		this.priority = priority;
		this.fAction = fAction;
	}
	
	public FlowRequest(Domain src, TrafficClass tClass, int priority,FlowAction fAction) {
		this.src = src;
		this.tClass = tClass;
		this.priority = priority;
		this.fAction = fAction;
	}
	
	//***SETTERS AND GETTERS
	public Domain getSrc() {
		return src;
	}
	public void setSrc(Domain src) {
		this.src = src;
	}
	public Domain getDst() {
		return dst;
	}
	public void setDst(Domain dst) {
		this.dst = dst;
	}
	public TrafficClass getTrafficClass() {
		return tClass;
	}
	public void setTrafficClass(TrafficClass tClass) {
		this.tClass = tClass;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public FlowAction getFlowAction() {
		return fAction;
	}
	public void setFlowAction(FlowAction fAction) {
		this.fAction = fAction;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result + ((fAction == null) ? 0 : fAction.hashCode());
		result = prime * result + priority;
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		result = prime * result + ((tClass == null) ? 0 : tClass.hashCode());
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
		FlowRequest other = (FlowRequest) obj;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
			return false;
		if (fAction != other.fAction)
			return false;
		if (priority != other.priority)
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		if (tClass == null) {
			if (other.tClass != null)
				return false;
		} else if (!tClass.equals(other.tClass))
			return false;
		return true;
	}
	
}
