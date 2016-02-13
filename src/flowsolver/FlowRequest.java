package flowsolver;

import java.io.Serializable;

public class FlowRequest implements Serializable{

	private static final long serialVersionUID = -5341413177306223467L;
	private Domain src;
	private Domain dst;
	private TrafficClass tClass;
	private int priority;
	private FlowAction fAction;
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
	public TrafficClass gettClass() {
		return tClass;
	}
	public void settClass(TrafficClass tClass) {
		this.tClass = tClass;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public FlowAction getfAction() {
		return fAction;
	}
	public void setfAction(FlowAction fAction) {
		this.fAction = fAction;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
