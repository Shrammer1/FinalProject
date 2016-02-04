package topology;

import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

public class HostTable extends ArrayList<SwitchMapping>{

	private static final long serialVersionUID = 853289804357492274L;
	
	public SwitchMapping getMapping(byte[] mac){
		
		for(SwitchMapping map:this){
			if(map.getMacs().contains(mac)) return map;
		}
		
		return null;
	}
	
	public String toString(){
		String retval = "";
		for(SwitchMapping map:this){
			for(byte[] mac:map.getMacs()){
				try {
					retval = retval + map.getSw().getSwitchFullName() + " : " + map.getPort() + " : " + Integer.toHexString(ByteBuffer.wrap(mac).getInt()) + "\n";
				} catch (RemoteException e) {
					//will never happen
				}
			}
		}
		return retval;
	}
	
	public synchronized boolean add(SwitchMapping map){
		synchronized (this) {
			this.remove(map);
			boolean retval = super.add(map);
			this.notifyAll();
			return retval;
		}
	}

	public void cleanDead() {
		synchronized (this) {
			Iterator<SwitchMapping> i = this.iterator();
			while(i.hasNext()){
				if(!(i.next().isValid())){
					i.remove();
				}
			}
			this.notifyAll();
		}
	}
	
	
}
