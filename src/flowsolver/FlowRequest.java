package flowsolver;

import java.io.Serializable;

public class FlowRequest implements Serializable{

	private static final long serialVersionUID = -5341413177306223467L;
	private Domain src;
	private Domain dst;
	private TrafficClass tClass;
	private int priority;
	private FlowAction fAction;
}
