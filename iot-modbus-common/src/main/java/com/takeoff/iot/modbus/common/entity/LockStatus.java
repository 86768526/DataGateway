package com.takeoff.iot.modbus.common.entity;

import lombok.Data;


@Data
public class LockStatus {

	/**
	 * 返回门锁号
	 * @return 门锁号
	 */
	private int lockNo;

	/**
	 * 返回门锁状态码
	 * @return 门锁状态码
	 */
	private int lockStatus;
	
	/**
	 * 返回传感器状态码
	 * @return 传感器状态码
	 */
	private int sensorStatus;
	
}
