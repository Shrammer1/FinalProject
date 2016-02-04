package topology;

import java.util.ArrayList;
import controller.OVSwitch;
import topology.SwitchMapping;

public class Link extends ArrayList<SwitchMapping>{

	private static final long serialVersionUID = 4557266269580220297L;
	
	public String toString(){
		String retval = "";
		
		for(SwitchMapping map:this){
			retval = retval + map.toString() + " - ";
		}
		
		return retval;
	}
	
	
	public void addSwitch(int port, OVSwitch sw, long ttl){
		this.add(new SwitchMapping(port, sw, ttl));
	}
	
	public int getPort(OVSwitch sw){
		for(SwitchMapping map : this){
			if(map.getSw() == sw){
				return map.getPort();
			}
		}
		return -1;
	}
	
	
}
