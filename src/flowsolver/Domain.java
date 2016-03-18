package flowsolver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;


public class Domain implements Serializable {


	private static final long serialVersionUID = -1827587933052636802L;
	private ArrayList<Domain> subDomains = new ArrayList<Domain>();
	private ArrayList<byte[]> networks = new ArrayList<byte[]>();
	private ArrayList<byte[]> macList = new ArrayList<byte[]>();
	private String name = "";
	
	//***CONSTRUCTORS***
	public Domain(ArrayList<Domain> subDomains, ArrayList<byte[]> networks, ArrayList<byte[]> macList) {
		this.subDomains = subDomains;
		this.networks = networks;
		this.macList = macList;
	}
	
	public Domain(String name, ArrayList<Domain> subDomains, ArrayList<byte[]> networks, ArrayList<byte[]> macList) {
		this.name = name;
		this.subDomains = subDomains;
		this.networks = networks;
		this.macList = macList;
	}
	
	public Domain(ArrayList<Domain> subDomains) {
		this.subDomains = subDomains;
	}

	public Domain(String name) {
		this.name = name;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((macList == null) ? 0 : macList.hashCode());
		result = prime * result + ((networks == null) ? 0 : networks.hashCode());
		result = prime * result + ((subDomains == null) ? 0 : subDomains.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Domain other = (Domain) obj;
		if (macList == null) {
			if (other.macList != null)
				return false;
		} else{
			for(byte[] arr1: macList){
				for(byte[] arr2:other.macList){
					if(!(Arrays.equals(arr1, arr2))){
						return false;
					}
				}
			}
		}
		if (networks == null) {
			if (other.networks != null)
				return false;
		} else {
			for(byte[] arr1: networks){
				for(byte[] arr2:other.networks){
					if(!(Arrays.equals(arr1, arr2))){
						return false;
					}
				}
			}
		}
		if (subDomains == null) {
			if (other.subDomains != null)
				return false;
		} else if (!subDomains.equals(other.subDomains))
			return false;
		return true;
	}
	
	
	
	
	//contains, equals, getAll net/macs/subdomains
}

