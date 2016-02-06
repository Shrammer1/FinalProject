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
	
	public synchronized void cleanDead() {
		/*
		synchronized (this) {
			Iterator<Link> i = this.iterator();
			while(i.hasNext()){
				if(!(i.next().isValid())){
					i.remove();
				}
			}
			this.notifyAll();
		}
		*/
		
		ArrayList<Link> tmp = new ArrayList<Link>(this);
		for(Link l:tmp){
			if(!(l.isValid())){
				this.remove(l);
			}
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
