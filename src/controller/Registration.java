package controller;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFType;

/*
 * Class to provide a data structure to store information about OFTypes (enum);
 * it contributes to their organization and/or mapping according to the needs
 * of the implementation.
 * 
 * OFType Enum Constant Summary:
 * HELLO, ERROR, ECHO_REQUEST, ECHO_REPLY, VENDOR, FEATURES_REQUEST,
 * FEATURES_REPLY, GET_CONFIG_REQUEST, GET_CONFIG_REPLY, SET_CONFIG, PACKET_IN,
 * FLOW_REMOVED, PORT_STATUS, PACKET_OUT, FLOW_MOD, PORT_MOD, STATS_REQUEST,
 * STATS_REPLY, BARRIER_REQUEST, BARRIER_REPLY
 * 
 */
public class Registration {
	
	/**************************************************
	 * PRIVATE VARIABLES
	 **************************************************/
	private String id;
	private ArrayList<OFType> types = new ArrayList<OFType>();
	private BlockingQueue<OFMessage> msgs = new LinkedBlockingQueue<OFMessage>();
	
	/**************************************************
	 * CONSTRUCTORS
	 **************************************************/
	//Minimal registration instantiation with ID only
	public Registration(String id){
		this.id = id;
	}
	
	//Simple registration instantiation with ID and a single OFType
	public Registration(String id, OFType type){
		this.id = id;
		this.types.add(type);
	}
	
	//Advanced registration instantiation with ID and several OFTypes
	public Registration(String id, ArrayList<OFType> types){
		this.id = id;
		this.types.addAll(types);
	}
	
	
	/**************************************************
	 * PUBLIC METHODS
	 **************************************************/
	public void addMsg(OFMessage msg){
		msgs.add(msg);
	}
	public OFMessage poll(){
		return msgs.poll();
	}
	public OFMessage take(){
		try {
			return msgs.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	public boolean msgsAvailable(){
		return msgs.isEmpty();
	}
	
	//Method to verify if the instance contains certain OFType
	public boolean contains(OFType type){
		if(types.contains(type)){
			return true;
		}
		return false;
	}
	
	/*
	 * Method to add an OFType to the instance and return boolean result
	 * of the operation
	 */
	public boolean register(OFType type){
		if(!(contains(type))){
			types.add(type);
			return true;
		}
		return false;
	}
	
	/*
	 * Method to add several OFTypes to an instance and return boolean result
	 * of the operation
	 */
	public boolean register(ArrayList<OFType> types){
		for(OFType t : types){
			if(this.types.contains(t)) return false;
		}
		types.addAll(types);
		return true;
	}
	
	/*
	 * Method to remove an OFType of an instance
	 */
	public boolean unregister(OFType type){
		return types.remove(type);
	}
	
	/*
	 * Method to remove several OFTypes of an instance
	 */
	public boolean unregister(ArrayList<OFType> types){
		return types.removeAll(types);
	}
	
	/*
	 * Method to obtain the ID of an instantiated object
	 */
	public String getID() {
		return id;
	}
}
