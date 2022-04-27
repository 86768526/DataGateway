package com.takeoff.iot.modbus.common.data;

import org.apache.commons.lang3.StringUtils;


public class MiiHeartBeatData extends MiiStringData {
	
	static final int DEVICE_GROUPNO_INDEX = 1;

	public MiiHeartBeatData(byte[] datas) {
		super(datas);
	}
	
	/**
	 * 返回设备组编码
	 * @return 设备编码
	 */
	public String deviceGroup() {
		return StringUtils.isBlank(content()) ? 
				String.valueOf(toBytes()[DEVICE_GROUPNO_INDEX]) : content();
	}
}
