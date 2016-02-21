package flowsolver;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import controller.Controller;
import controller.OFSwitch;

public class FlowSolver {
	private Controller controller;
	
	public FlowSolver(Controller ctrl){
		this.controller = ctrl;
	}
	
	public boolean requestAddFlow(FlowRequest request, int id){
		
		//TODO: most of this code should be ported out to a separate method so its reusable, the only stuff that should stay in here should be actually 
		//determining which FlowEntry objects to keep and which to update
		
		//Step 1: break the request down into all the sub components
		//Step 2: determine the flows needed to build all the FlowEntry objects
		//Step 3: add the FlowEntry objects to the FlowEntryTable, updating the FlowRequest field where appropriate and only adding new FlowEntry objects when there are no duplicates (done via HashMap)
		
		ArrayList<FlowEntry> flows = buildFlows(request, id);
		if(flows == null) return false;
		
		
		return true;
	}
	
	private ArrayList<FlowEntry> buildFlows(FlowRequest request, int id){
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
			//TODO: replace this with a throw exception
			return null;
		}
		//add the requested priority to the applications id
		priority = priority + id;
		
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
			else if(entry.getType() == DomainType.MAC){
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
				else if(entry.getType() == DomainType.MAC){
					dstMACs.addAll(entry.getValues());
				}
			}
		}
		else{
			wildcardDstFlag=true;
		}
		
		
		//Step 2:
		
		//No source specified
		if(srcIPs.size()==0 && srcMACs.size()==0){
			return null;
		}
		
		ArrayList<FlowEntry> flows = new ArrayList<FlowEntry>();
		//first create flow entries for IPs then MACs
		if(wildcardDstFlag){
			if(srcIPs.size()!=0){
				flows.addAll(generateFlows(srcIPs, priority));
			}
			if(srcMACs.size() !=0){
				
			}
		}
		else{
			if(srcIPs.size()!=0){
				if(dstIPs.size()!=0){
					
				}
				if(dstMACs.size() !=0){
					
				}
			}
			if(srcMACs.size() !=0){
				if(dstIPs.size()!=0){
					
				}
				if(dstMACs.size() !=0){
					
				}
			}
		}
		
		for(FlowEntry entry:flows){
			entry.addFlowRequest(request);
		}
		
		return flows;
	}
	
	/**
	 * Generates an ArrayList of FlowEntry objects for a given source with a specified priority 
	 * @param src Source byte arrays
	 * @param priority Priority of the FlowEntry
	 * @return The resulting ArayList of FlowEntry objects
	 */
	private ArrayList<FlowEntry> generateFlows(ArrayList<byte[]> srcList, int priority){
		ArrayList<FlowEntry> retVal = new ArrayList<FlowEntry>(); 
		boolean srcIsMAC = false;
		if(srcList.get(0).length == 6){
			srcIsMAC = true;
		}
		
		if(srcIsMAC){
			for(byte[] src:srcList){
				FlowEntry entry = new FlowEntry();
				entry.setActive(false);
				entry.setSwitch(controller.getTopologyMapper().getMapping(src));
				//TODO: build the OFFlowMod
			}
		}
		else{
			for(byte[] src:srcList){
				FlowEntry entry = new FlowEntry();
				entry.setActive(false);
				if(src.length == 4){
					//src is a single IP
					entry.setSwitch(controller.getTopologyMapper().getMapping(ByteBuffer.wrap(src).getInt()));
					//TODO: build the OFFlowMod
				}
				else if(src.length == 5){
					ArrayList<OFSwitch> switches = controller.getTopologyMapper().getMappings(ByteBuffer.wrap(src).getInt(), (int) src[4]);
					//TODO: build the OFFlowMod
				}
			}
		}
		
		
		
		
		return retVal;
	}
	
	/**
	 * Generates an ArrayList of FlowEntry objects for a given source and destination with a specified priority 
	 * @param src Source byte arrays
	 * @param dst Destination byte arrays
	 * @param priority Priority of the FlowEntry
	 * @return The resulting ArayList of FlowEntry objects
	 */
	private ArrayList<FlowEntry> generateFlows(ArrayList<byte[]> srcList,ArrayList<byte[]> dstList, int priority){
		boolean srcIsMAC = false;
		boolean dstIsMAC = false;
		if(srcList.get(0).length == 6){
			srcIsMAC = true;
		}
		if(dstList.get(0).length == 6){
			srcIsMAC = true;
		}
		
		
		return null;
	}
	
	
	
	
	
	
	public boolean requestDelFlow(FlowRequest request, int id){
		return false;
	}

}
