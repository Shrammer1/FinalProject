package flowSolver;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Domain implements Serializable {

	private ArrayList<Domain> subDomains = new ArrayList<Domain>();
	private ArrayList<byte[]> networks = new ArrayList<byte[]>();
	private ArrayList<byte[]> macList = new ArrayList<byte[]>();
	
	//contains, equals, getAll net/macs/subdomains
}

