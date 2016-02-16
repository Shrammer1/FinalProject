package flowsolver;

import java.io.Serializable;

public class TrafficClass implements Serializable{

	private static final long serialVersionUID = -7272204048173591835L;
	private int tcpPortSrc;
	private int tcpPortDst;
	private int udpPortSrc;
	private int udpPortDst;
	private PortOpt portType;
	
	//***CONSTRUCTORS
	public TrafficClass(int src, int dst, PortOpt portType) {
		if (portType == PortOpt.TCP)	
		{
			this.tcpPortSrc = src;
			this.tcpPortDst = dst;
		}
		else if (portType == PortOpt.UDP)
		{
			this.udpPortSrc = src;
			this.udpPortDst = dst;
		}
		else
		{
			this.tcpPortSrc = 0;
			this.tcpPortDst = 0;
			this.udpPortSrc = 0;
			this.udpPortDst = 0;
		}
		
		this.portType = portType;
	}

	
	//***GETTERS
	public int getTcpPortSrc() {
		return tcpPortSrc;
	}
	
	public int getTvpPortDst() {
		return tcpPortDst;
	}
	
	public int getUdpPortSrc() {
		return udpPortSrc;
	}
	
	public int getUdpPortDst() {
		return udpPortDst;
	}
	
	public PortOpt getportType() {
		return portType;
	}
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
