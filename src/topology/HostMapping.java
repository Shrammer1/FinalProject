package topology;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class HostMapping {
	public byte[] mac;
	public int ip;
	private long ttl;
	private long created = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
	
	public boolean isValid(){
		if(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - created > ttl){
			return false;
		}
		return true;
	}
	
	public void update(HostMapping map){
		if(map.ip != 0){
			this.ip = map.ip;
			created = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		}
	}
	
	public HostMapping(byte[] mac, int ip,long ttl){
		this.mac = mac;
		this.ip = ip;
		this.ttl = ttl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(mac);
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
		HostMapping other = (HostMapping) obj;
		if (Arrays.equals(mac, other.mac)){
			return true;
		}
		else{
			return false;
		}
		
	}
	
	
	
	
}
