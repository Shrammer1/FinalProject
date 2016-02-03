package topology;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class LLDPMessage {
	
	private byte[] dstMac;
	private byte[] srcMac;
	private byte[] ethertype;
	private byte[] chasisID;
	private byte[] portID;
	private byte[] ttl;
	private byte[] eof;
	private int port;
	private String switchID;
	private ArrayList<byte[]> optionalTLVs;
	
	
	public LLDPMessage(String switchID, int i){
		this.port = i;
		this.switchID = switchID;
	}
	
	public LLDPMessage(byte[] packetData) {
		parseMessage(packetData);
	}

	/**
	 * @return Returns the length of the LLDP message in bytes
	 */
	public int length(){
		int retval = 0;
		retval +=dstMac.length;
		retval +=srcMac.length;
		retval +=ethertype.length;
		retval +=chasisID.length;
		retval +=portID.length;
		retval +=ttl.length;
		retval +=eof.length;
		return retval;
	}
	
	public void parseMessage(byte[] msgIn){
		ByteBuffer msg = ByteBuffer.wrap(msgIn);
		dstMac = new byte[6];
		srcMac = new byte[6];
		ethertype = new byte[2];
		msg.get(dstMac, 0, 6);
		msg.get(srcMac, 0, 6);
		msg.get(ethertype, 0, 2);
		TLV chasisID = new TLV(msg);
		TLV portID = new TLV(msg);
		TLV ttl = new TLV(msg);
		TLV nextTLV = null;
		do{
			nextTLV = new TLV(msg);
			if(nextTLV.getType() != 0 ){
				optionalTLVs.add(nextTLV.toArray());
			}
		}while(nextTLV.getType() != 0);
		TLV eof = new TLV(nextTLV);
		
		this.chasisID = chasisID.toArray();
		this.portID = portID.toArray();
		this.ttl = ttl.toArray();
		this.eof = eof.toArray();
		this.switchID = new String(chasisID.getData());
		this.port = ByteBuffer.wrap(portID.getData()).getShort();
	}
	
	public int getPort(){
		return port;
	}
	public String getSwitchID(){
		return switchID;
	}
	
	public byte[] getMessage(){
		
		
		ByteBuffer dstMac = ByteBuffer.allocate(6);
		dstMac.put((byte) 0x01);
		dstMac.put((byte) 0x80);
		dstMac.put((byte) 0xc2);
		dstMac.put((byte) 0x00);
		dstMac.put((byte) 0x00);
		dstMac.put((byte) 0x00);
		dstMac.rewind();
		this.dstMac = dstMac.array();
		
		
		/*
		//16-0A-64-1A-95-B9 TEST MAC ADDR
		
		ByteBuffer srcMac = ByteBuffer.allocate(6);
		srcMac.put((byte) 0x16);
		srcMac.put((byte) 0x0a);
		srcMac.put((byte) 0x64);
		srcMac.put((byte) 0x1a);
		srcMac.put((byte) 0x95);
		srcMac.put((byte) 0xb9);
		srcMac.rewind();
		this.srcMac = srcMac.array();
		*/
		
		
		//Actual MAC
		ByteBuffer srcMac = ByteBuffer.allocate(6);
		
		srcMac.put(Integer.decode("0x" + switchID.substring(4, 6)).byteValue());
		srcMac.put(Integer.decode("0x" + switchID.substring(6, 8)).byteValue());
		srcMac.put(Integer.decode("0x" + switchID.substring(8, 10)).byteValue());
		srcMac.put(Integer.decode("0x" + switchID.substring(10, 12)).byteValue());
		srcMac.put(Integer.decode("0x" + switchID.substring(12, 14)).byteValue());
		srcMac.put(Integer.decode("0x" + switchID.substring(14)).byteValue());
		srcMac.rewind();
		this.srcMac = srcMac.array();
		
		
		//Ethertype = 0x88CC
		
		ByteBuffer ethertype = ByteBuffer.allocate(2);
		ethertype.put((byte) 0x88);
		ethertype.put((byte) 0xcc);
		ethertype.rewind();
		this.ethertype = ethertype.array();
		
		
		TLV chasisID = new TLV((byte) 1,switchID.getBytes());
		TLV portID = new TLV((byte) 2, ByteBuffer.allocate(4).putInt(port).array());
		TLV ttl = new TLV((byte) 3,new byte[]{(byte) 0,(byte) 120});
		
		this.chasisID = chasisID.toArray();
		this.portID = portID.toArray();
		this.ttl = ttl.toArray();
		
		TLV eof = new TLV((byte) 0);
		this.eof = eof.toArray();
		
		
		//make sure to allocate as much space as needed and then put all the other messages into a single buffer
		ByteBuffer outMsg = null;
		outMsg = ByteBuffer.allocate(this.length());
		outMsg.put(dstMac);
		outMsg.put(srcMac);
		outMsg.put(ethertype);
		outMsg.put(this.chasisID);
		outMsg.put(this.portID);
		outMsg.put(this.ttl);
		outMsg.put(this.eof);
		
		
		
		//rewind the buffer
		outMsg.rewind();
		
		
		//apparently the FCS is automatically added by the vSwitch, who knew?
		
		//create new CRC32 object
		//CRC32 crc = new CRC32();
		//crc.reset();
		//update the CRC object to calculate the crc code
		//crc.update(outMsg.array(),0,outMsg.array().length);
		//get the value of the crc code for the fcs
		
		
		//fcs = ByteBuffer.allocate(4).putInt((int) crc.getValue()).array();
		
		ByteBuffer finalMsg = ByteBuffer.allocate(outMsg.array().length );//+ fcs.length);
		finalMsg.put(outMsg.array());
		//finalMsg.put(fcs);
		
		return finalMsg.array();
	}	
}
	
	