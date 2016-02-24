package flowsolver;

import java.io.Serializable;

public class TrafficClass implements Serializable{

	private static final long serialVersionUID = -7272204048173591835L;
	private short srcPort;
	private short dstPort;
	private byte portType;
	
	/**
	 * Default TrafficClass. All traffic will match this
	 */
	public TrafficClass(){
		this.srcPort=0;
		this.dstPort=0;
		this.portType=0;
	}
	
	//***CONSTRUCTORS
	public TrafficClass(short src, short dst, byte portType) {
		this.srcPort=src;
		this.dstPort=dst;
		
		this.portType = portType;
	}
	
	
	//***GETTERS

	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public short getSrcPort() {
		return srcPort;
	}

	public void setSrcPort(short srcPort) {
		this.srcPort = srcPort;
	}

	public short getDstPort() {
		return dstPort;
	}

	public void setDstPort(short dstPort) {
		this.dstPort = dstPort;
	}

	public byte getPortType() {
		return portType;
	}

	public void setPortType(byte portType) {
		this.portType = portType;
	}
}
