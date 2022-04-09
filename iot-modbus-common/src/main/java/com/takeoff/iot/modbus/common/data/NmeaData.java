package com.takeoff.iot.modbus.common.data;

import org.apache.commons.lang3.StringUtils;

public class NmeaData extends MiiStringData {
	static final int DEVICE_GROUPNO_INDEX = 1;
	public NmeaData(byte[] datas){
		super(datas);
	}

	public String deviceGroup() {
		return StringUtils.isBlank(content()) ?
				String.valueOf(toBytes()[DEVICE_GROUPNO_INDEX]) : content();
	}

	public int command() {
		return 99;
	}
}
