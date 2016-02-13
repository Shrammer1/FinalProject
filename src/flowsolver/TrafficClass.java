package flowsolver;

import java.io.Serializable;

public class TrafficClass implements Serializable{

	private static final long serialVersionUID = -7272204048173591835L;
	private int tcpPortSrc;
	private int tcPortDst;
	private int udpPortSrc;
	private int udpPortDst;
	public int getTcpPortSrc() {
		return tcpPortSrc;
	}
	public void setTcpPortSrc(int tcpPortSrc) {
		this.tcpPortSrc = tcpPortSrc;
	}
	public int getTvpPortDst() {
		return tvpPortDst;
	}
	public void setTvpPortDst(int tvpPortDst) {
		this.tvpPortDst = tvpPortDst;
	}
	public int getUdpPortSrc() {
		return udpPortSrc;
	}
	public void setUdpPortSrc(int udpPortSrc) {
		this.udpPortSrc = udpPortSrc;
	}
	public int getUdpPortDst() {
		return udpPortDst;
	}
	public void setUdpPortDst(int udpPortDst) {
		this.udpPortDst = udpPortDst;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	
}
