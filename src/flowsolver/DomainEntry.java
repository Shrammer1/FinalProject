package flowsolver;

import java.util.ArrayList;

public class DomainEntry {
	private ArrayList<byte []> values;
	private DomainType type;
	
	public ArrayList<byte[]> getValues() {
		return values;
	}

	public DomainType getType() {
		return type;
	}

	public DomainEntry(ArrayList<byte[]> values, DomainType type){
		this.values = values;
		this.type = type;
	}
	
}
