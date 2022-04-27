package com.takeoff.iot.modbus.common.data;

import org.apache.commons.lang3.ArrayUtils;


public class MiiStringData extends MiiSlotData {
	
	protected String content;
	
	public MiiStringData(byte[] datas) {
		super(datas);
		if(length() > CONTENT_INDEX){
			content = new String(ArrayUtils.subarray(datas, CONTENT_INDEX, length()));
		}else{
			content = null;
		}
	}
	
	/**
	 * 返回柜体的反馈内容信息
	 * @return 反馈内容信息
	 */
	public String content() {
		return content;
	}
}
