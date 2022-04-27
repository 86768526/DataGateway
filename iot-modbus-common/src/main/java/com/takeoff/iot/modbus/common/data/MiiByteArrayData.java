package com.takeoff.iot.modbus.common.data;


public class MiiByteArrayData implements MiiData {
	
	protected byte[] datas;
	
	public MiiByteArrayData(byte[] datas) {
		this.datas = datas;
	}
	
	public byte[] toBytes() {
		return datas;
	}
	
	public int length() {
		return datas.length;
	}

}
