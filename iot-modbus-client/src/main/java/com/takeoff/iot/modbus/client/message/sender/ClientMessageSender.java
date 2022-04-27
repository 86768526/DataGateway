package com.takeoff.iot.modbus.client.message.sender;

import java.util.List;

import com.takeoff.iot.modbus.common.data.Finger;


public interface ClientMessageSender {

	/**
	 * 上传设备组指令.
	 * @param deviceGroup 设备组号
	 */
	void registerGroup(String deviceGroup);

	/**
	 * 上传控制单锁指令.
	 * @param device 设备号
	 * @param status 锁状态
	 */
	void unlock(int device, int status);


}
