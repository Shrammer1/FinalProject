package topology;

import java.util.ArrayList;
import java.util.Iterator;

import controller.OFSwitch;
import topology.SwitchMapping;

public class Link extends ArrayList<SwitchMapping>{

	private static final long serialVersionUID = 4557266269580220297L;
	
	public String toString(){
		String retval = "";
		
		for(SwitchMapping map:this){
			retval = retval + map.toString() + " - ";
		}
		
		if(retval.equals("")) retval = "Removing dead link (no mappings)";
		return retval;
	}
	
	public boolean equals(Object l){
		return super.equals(l);
	}
		
	public void addSwitch(int port, OFSwitch sw, long ttl){
		this.add(new SwitchMapping(port, sw, ttl));
	}
	
	public int getPort(OFSwitch sw){
		for(SwitchMapping map : this){
			if(map.getSw() == sw){
				return map.getPort();
			}
		}
		return -1;
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

	public boolean isValid() {
		if(super.size()==0){
			return false;
		}
		this.cleanDead();
		return true;
	}
	
}
