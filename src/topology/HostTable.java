package topology;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import controller.OVSwitch;

public class HostTable extends ArrayList<SwitchMapping>{

	private static final long serialVersionUID = 853289804357492274L;
	
	public SwitchMapping getMapping(int inPort, OVSwitch sw){
		SwitchMapping testMap = new SwitchMapping(inPort, sw);
		for(SwitchMapping map:this){
			if(map.equals(testMap)) return map;
		}
		
		return null;
	}
	
	public boolean remove(Object o){
		//System.out.println("Removing host mapping: " + ((SwitchMapping) o).toString());
		return super.remove(o);
	}
	
	public String toString(){
		String retval = "";
		for(SwitchMapping map:this){
			for(HostMapping h:map.getHosts()){
				try {
					//retval = retval + map.getSw().getSwitchFullName() + " : " + map.getPort() + " : " + Integer.toHexString(ByteBuffer.wrap(mac).getInt()) + "\n";
					retval = retval + map.getSw().getSwitchFullName() + " : " + map.getPort() + " : " + bytesToString(h.mac) + " : " + intToIP(h.ip) + "\n";
				} catch (RemoteException e) {
					//will never happen
				}
			}
		}
		return retval;
	}
	
	private String intToIP(int ip){
	  byte[] addr = new byte[] {
	    (byte)((ip >>> 24) & 0xff),
	    (byte)((ip >>> 16) & 0xff),
	    (byte)((ip >>>  8) & 0xff),
	    (byte)((ip       ) & 0xff)};

	  try {
		return InetAddress.getByAddress(addr).getHostAddress();
	  } catch (UnknownHostException e) {
		e.printStackTrace();
	  }
	  return null;
	}
	
	
	//TODO: make this a public static class/method
	private String bytesToString(byte[] mac){
		StringBuilder sb = new StringBuilder(18);
	    for (byte b : mac) {
	        if (sb.length() > 0)
	            sb.append(':');
	        sb.append(String.format("%02x", b));
	    }
	    return sb.toString();
	}
	
	
	public synchronized boolean add(SwitchMapping map){
		for(SwitchMapping m:this){
			if(m.equals(map)){
				m.updateHosts(map.getHosts());
				return true;
			}
		}
		return super.add(map);
	}

	public void cleanDead() {
				
		ArrayList<SwitchMapping> tmp = new ArrayList<SwitchMapping>(this);
		for(SwitchMapping map:tmp){
			if(!(map.isValid())){
				this.remove(map);
			}
		}
		
		
		
		
	}
	
	
}
