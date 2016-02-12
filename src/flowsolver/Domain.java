package flowsolver;

import java.io.Serializable;
import java.util.ArrayList;


public class Domain implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -1827587933052636802L;
	private ArrayList<Domain> subDomains = new ArrayList<Domain>();
	private ArrayList<byte[]> networks = new ArrayList<byte[]>();
	private ArrayList<byte[]> macList = new ArrayList<byte[]>();
	
	//contains, equals, getAll net/macs/subdomains
}

