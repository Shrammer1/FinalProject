package topology;

import java.util.ArrayList;
import controller.OVSwitch;
import topology.SwitchMapping;

public class Link extends ArrayList<SwitchMapping>{

	private static final long serialVersionUID = 4557266269580220297L;

	public void addSwitch(int port, OVSwitch sw){
		this.add(new SwitchMapping(port, sw));
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
