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
	public FlowRequest(Domain src, Domain dst, TrafficClass tClass,
			int priority, FlowAction fAction) {
		this.src = src;
		this.dst = dst;
		this.tClass = tClass;
		this.priority = priority;
		this.fAction = fAction;
	}
	
	public FlowRequest(Domain src, TrafficClass tClass, int priority,
			FlowAction fAction) {
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
