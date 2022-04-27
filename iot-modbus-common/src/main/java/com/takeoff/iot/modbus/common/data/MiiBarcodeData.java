package com.takeoff.iot.modbus.common.data;

import com.takeoff.iot.modbus.common.utils.BytesToHexUtil;
import org.apache.commons.lang3.ArrayUtils;


public class MiiBarcodeData extends MiiHexData {

	public MiiBarcodeData(byte[] datas) {
		super(datas);
		byte[] barcodeByte = ArrayUtils.subarray(datas, CONTENT_INDEX, datas.length);
		content = BytesToHexUtil.asciiToString(barcodeByte);
	}

}
