package com.archermind.txtbl.pushmail.timer;




public class BytePacket {
	

    public static final char HEARBEAT = 'H';
    public static final char NOTIFY = 'Y';
    public static final char RESPONSE = 'S';

	
	public static byte[] getHeartbeatByte(String interval) {
		byte[] heartbeat = new byte[2];
		heartbeat[0]=HEARBEAT;
		heartbeat[1]=Byte.parseByte(interval);
		return heartbeat;
	}
	
	public  static byte[] getNotifyByte() {
		byte[] notify = new byte[2];
		notify[0]=NOTIFY;
		notify[1]=0x00;
		return notify;
	}
	
	public static byte[] getResponseByte(){
		byte[] res = new byte[2];
		res[0]=RESPONSE;
		res[1]=0x00;
		return res;
	}
	

}
