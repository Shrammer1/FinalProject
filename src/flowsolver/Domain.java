package flowsolver;

import java.io.Serializable;
import java.util.ArrayList;


public class Domain implements Serializable {


	private static final long serialVersionUID = -1827587933052636802L;
	private ArrayList<Domain> subDomains = new ArrayList<Domain>();
	private ArrayList<byte[]> networks = new ArrayList<byte[]>();
	private ArrayList<byte[]> macList = new ArrayList<byte[]>();
	
	//***CONSTRUCTORS***
	public Domain(ArrayList<Domain> subDomains, ArrayList<byte[]> networks, ArrayList<byte[]> macList) {
		this.subDomains = subDomains;
		this.networks = networks;
		this.macList = macList;
	}
	
	
	public Domain(ArrayList<Domain> subDomains) {
		this.subDomains = subDomains;
	}


	public Domain() {
		
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
	
	
	//Utility methods
	
	/**
	 * Converts this domain and all child domains into an ArrayList of DomainEntry objects
	 * @return The ArrayList of DomainEntry objects
	 */
	public ArrayList<DomainEntry> toArray(){
		return parseDomains(this);
	}
	
	private ArrayList<DomainEntry> parseDomains(Domain head){
		ArrayList<DomainEntry> retVal = new ArrayList<DomainEntry>();
		retVal.add(new DomainEntry(head.networks, DomainType.IP));
		retVal.add(new DomainEntry(head.macList, DomainType.MAC));
		for(Domain domain:head.subDomains){
			retVal.addAll(parseDomains(domain));
		}
		return retVal;
	}
	
	
	
	
	//contains, equals, getAll net/macs/subdomains
}

