package topology;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class IPMapping {
	private int ipaddr;
	private long created = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
	private boolean timingOut = false;
	private HostMapping host;
	
	public IPMapping(int ip){
		this.setIP(ip);
	}
	
	public IPMapping(int ip, HostMapping host){
		this.setIP(ip);
		this.setHost(host);
	}
	
	public int getIP() {
		return ipaddr;
	}

	public void setIP(int ipaddr) {
		this.ipaddr = ipaddr;
	}
	
	
	public void startTimingOut(){
		timingOut = true;
		created = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
	}
	
	public void refresh(){
		timingOut=false;
	}
	
	public boolean isValid(){
		if(timingOut==false) return true;
		if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - created > 30){
			
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ipaddr;
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
		IPMapping other = (IPMapping) obj;
		if (ipaddr != other.ipaddr)
			return false;
		return true;
	}

	public HostMapping getHost() {
		return host;
	}

	public void setHost(HostMapping host) {
		this.host = host;
	}

	public void updateSwitches() {
		HostMapping hostToCheck = new HostMapping();
		ArrayList<IPMapping> ips = new ArrayList<IPMapping>();
		ips.add(this);
		hostToCheck.setIPs(ips);
		host.getSwitchMap().getHostTable().getTopology().getController().getFlowSolver().removeIfAble(host.getSwitchMap().getHostTable().getTopology().getController().getFlowSolver().getRelevantFlows(hostToCheck));
	}
	
	
	
}
