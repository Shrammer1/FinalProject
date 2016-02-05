package topology;

import java.util.ArrayList;
import java.util.Iterator;

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
	
	public void cleanDead() {
		synchronized (this) {
			Iterator<Link> i = this.iterator();
			while(i.hasNext()){
				if(!(i.next().isValid())){
					i.remove();
				}
			}
			this.notifyAll();
		}
	}
	
	
	
	public String toString(){
		
		String retval = "";
		
		for(Link l:this){
			retval = retval + l.toString() + "\n";
		}
		
		return retval;
	}
	
}
