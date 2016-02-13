package flowsolver;

import java.io.Serializable;
import java.util.ArrayList;


public class Domain implements Serializable {


	private static final long serialVersionUID = -1827587933052636802L;
	private ArrayList<Domain> subDomains = new ArrayList<Domain>();
	private ArrayList<byte[]> networks = new ArrayList<byte[]>();
	private ArrayList<byte[]> macList = new ArrayList<byte[]>();
	
	//***CONSTRUCTORS***
	public Domain(ArrayList<Domain> subDomains, ArrayList<byte[]> networks,
			ArrayList<byte[]> macList) {
		this.subDomains = subDomains;
		this.networks = networks;
		this.macList = macList;
	}
	
	
	public Domain(ArrayList<Domain> subDomains) {
		this.subDomains = subDomains;
	}


//***GETTERS AND SETTERS
	public ArrayList<Domain> getSubDomains() {
		return subDomains;
	}
	public void setSubDomains(ArrayList<Domain> subDomains) {
		this.subDomains = subDomains;
	}
	public ArrayList<byte[]> getNetworks() {
		return networks;
	}
	public void setNetworks(ArrayList<byte[]> networks) {
		this.networks = networks;
	}
	public ArrayList<byte[]> getMacList() {
		return macList;
	}
	public void setMacList(ArrayList<byte[]> macList) {
		this.macList = macList;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
	
	//contains, equals, getAll net/macs/subdomains
}

