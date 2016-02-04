package topology;

import java.util.ArrayList;

import controller.OVSwitch;

public class LinkTable extends ArrayList<Link> {

	private static final long serialVersionUID = 1220508259400532264L;
	
	public Link getLink(int port, OVSwitch sw){
		for(Link l:this){
			if(l.contains(new SwitchMapping(port,sw))){
				return l;
			}
		}
		return null;
	}
	
	
}
