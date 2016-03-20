package flowsolver;

import java.io.Serializable;

public class TrafficClass implements Serializable{

	public static final byte PORTTYPE_ANY = 0; // indicates ports are ignored when classifying the traffic
	public static final byte PORTTYPE_TCP = 6;
	public static final byte PORTTYPE_UDP = 17;
	
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
		this.portType=PORTTYPE_ANY;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dstPort;
		result = prime * result + portType;
		result = prime * result + srcPort;
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
		TrafficClass other = (TrafficClass) obj;
		if (dstPort != other.dstPort)
			return false;
		if (portType != other.portType)
			return false;
		if (srcPort != other.srcPort)
			return false;
		return true;
	}
}
