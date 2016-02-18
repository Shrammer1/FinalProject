package flowsolver;

import java.util.ArrayList;

import controller.Controller;

public class FlowSolver {
	private Controller controller;
	
	public FlowSolver(Controller ctrl){
		this.controller = ctrl;
	}
	
	public boolean requestAddFlow(FlowRequest request, int id){
		
		boolean wildcardDstFlag = false;
		PortOpt portType;
		int srcPortNum = 0;
		int dstPortNum = 0; 
		FlowAction action;
		int priority;
		
		portType = request.getTrafficClass().getPortType();
		if(portType == PortOpt.NONE){
			srcPortNum = 0;
			dstPortNum = 0; 
		}
		else if(portType == PortOpt.TCP){
			srcPortNum = request.getTrafficClass().getTcpPortSrc();
			dstPortNum = request.getTrafficClass().getTcpPortDst();
		}
		else if(portType == PortOpt.UDP){
			srcPortNum = request.getTrafficClass().getUdpPortSrc();
			dstPortNum = request.getTrafficClass().getUdpPortDst();
		}
		
		action = request.getFlowAction();
		priority = request.getPriority();
		
		//if the application asks for a priority that is out of the valid range reject the flow.
		if(priority < 0 || priority > 100){
			return false;
		}
		//add the requested priority to the applications priority
		priority = priority + id;
		
		//Step 1: break the request down into all the sub components
		//Step 2: determine the flows needed to build all the FlowEntry objects 
		//Step 3: build FlowEntry objects for each flow required 
		//Step 4: determine the minimum number of FlowEntry objects required to accomplish the requested flow
		//Step 5: add the FlowEntry objects to the FlowEntryTable, updating the FlowRequest field where appropriate and only adding new FlowEntry objects when there are no duplicates (done via HashMap)
		
		//Step 1:
		
		ArrayList<DomainEntry> listOfDomainEntries = request.getSrc().toArray();
		ArrayList<byte[]> srcIPs = new ArrayList<byte[]>();
		ArrayList<byte[]> srcMACs = new ArrayList<byte[]>();
		ArrayList<byte[]> dstIPs = new ArrayList<byte[]>();
		ArrayList<byte[]> dstMACs = new ArrayList<byte[]>();
		
		for(DomainEntry entry:listOfDomainEntries){
			if(entry.getType() == DomainType.IP){
				srcIPs.addAll(entry.getValues());
			}
			else if(entry.getType() == DomainType.Mac){
				srcMACs.addAll(entry.getValues());
			}
		}
		
		listOfDomainEntries = request.getDst().toArray();
		//if there are 1 or more domain entries in the dst field load them, else set the wildcard flag
		if(listOfDomainEntries.size() != 0){
			for(DomainEntry entry:listOfDomainEntries){
				if(entry.getType() == DomainType.IP){
					dstIPs.addAll(entry.getValues());
				}
				else if(entry.getType() == DomainType.Mac){
					dstMACs.addAll(entry.getValues());
				}
			}
		}
		else{
			wildcardDstFlag=true;
		}
		
		
		
		
		
		
		return false;
	}
	
	public boolean requestDelFlow(FlowRequest request, int id){
		return false;
	}

}
