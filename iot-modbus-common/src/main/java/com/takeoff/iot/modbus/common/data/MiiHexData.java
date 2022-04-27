package com.takeoff.iot.modbus.common.data;
import org.bouncycastle.util.encoders.Hex;


public class MiiHexData extends MiiStringData {

	public MiiHexData(byte[] datas) {
		super(datas);
		content = Hex.toHexString(datas, CONTENT_INDEX, length() - CONTENT_INDEX);
	}
	
}
