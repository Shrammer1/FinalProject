package controller;
import java.util.ArrayList;

import org.openflow.protocol.OFType;


public class Registration {
	private String id;
	private ArrayList<OFType> types = new ArrayList<OFType>();
	
	public Registration(String id){
		this.id = id;
	}
	
	public Registration(String id, OFType type){
		this.id = id;
		this.types.add(type);
	}
	
	public Registration(String id, ArrayList<OFType> types){
		this.id = id;
		this.types.addAll(types);
	}
	
	
	public boolean contains(OFType type){
		if(types.contains(type)){
			return true;
		}
		return false;
	}
	
	public boolean register(OFType type){
		if(!(contains(type))){
			types.add(type);
			return true;
		}
		return false;
	}
	public boolean register(ArrayList<OFType> types){
		for(OFType t : types){
			if(this.types.contains(t)) return false;
		}
		types.addAll(types);
		return true;
	}
	
	public boolean unregister(OFType type){
		return types.remove(type);
	}
	
	public boolean unregister(ArrayList<OFType> types){
		return types.removeAll(types);
	}
	
	
	public String getID() {
		return id;
	}
	
	
	
	
}
