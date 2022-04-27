package com.takeoff.iot.modbus.netty.channel;

import com.takeoff.iot.modbus.common.message.MiiMessage;
import com.takeoff.iot.modbus.netty.service.NameValue;


public interface MiiChannel extends NameValue {
	
	/**
	 * 发送指令信息
	 * @param msg 指令信息
	 */
	void send(MiiMessage msg);
	
}
