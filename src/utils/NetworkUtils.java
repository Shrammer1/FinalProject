package utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class NetworkUtils {
	
	private NetworkUtils(){
		
	}
	
	public static String bytesToMac(byte[] mac){
		StringBuilder sb = new StringBuilder(18);
	    for (byte b : mac) {
	        if (sb.length() > 0)
	            sb.append(':');
	        sb.append(String.format("%02x", b));
	    }
	    return sb.toString();
	}
	
	public static String intToIP(int ip){
	  byte[] addr = new byte[] {
	    (byte)((ip >>> 24) & 0xff),
	    (byte)((ip >>> 16) & 0xff),
	    (byte)((ip >>>  8) & 0xff),
	    (byte)((ip       ) & 0xff)};

	  try {
		return InetAddress.getByAddress(addr).getHostAddress();
	  } catch (UnknownHostException e) {
		e.printStackTrace();
	  }
	  return null;
	}
	
}
