package topology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.openflow.protocol.OFMatch;

public class MacMapping {
	private byte[] macAddr;
	private ArrayList<OFMatch> matches = new ArrayList<OFMatch>(); 
	private long created = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
	private boolean timingOut = false;
	private long ttl;
	
	public boolean addMatch(OFMatch match){
		return matches.add(match);
	}
	public boolean removeMatch(OFMatch match){
		boolean retVal = matches.remove(match);
		if(matches.size()==0) startTimingOut();
		return retVal;
	}
	
	public MacMapping(byte[] mac, long ttl){
		this.setMacAddress(mac);
		this.ttl = ttl;
	}
	
	public MacMapping(byte[] mac, long ttl, OFMatch match){
		this.setMacAddress(mac);
		this.matches.add(match);
		this.ttl = ttl;
	}
	
	
	private void startTimingOut(){
		timingOut = true;
		created = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
	}
	
	public void refresh(){
		timingOut=false;
	}
	
	public boolean isValid(){
		if(timingOut==false) return true;
		if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - created > ttl){
			return false;
		}
		return true;
	}

	public byte[] getMacAddress() {
		return macAddr;
	}

	public void setMacAddress(byte[] macAddr) {
		this.macAddr = macAddr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(macAddr);
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
		MacMapping other = (MacMapping) obj;
		if (!Arrays.equals(macAddr, other.macAddr))
			return false;
		return true;
	}
	public ArrayList<OFMatch> getMatches() {
		return matches;
	}
	public void addMatchs(ArrayList<OFMatch> matches2) {
		matches.addAll(matches2);
	}

	
}

	
	
	

