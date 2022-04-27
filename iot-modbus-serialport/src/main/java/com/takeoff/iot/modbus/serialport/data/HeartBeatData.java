package com.takeoff.iot.modbus.serialport.data;

import com.takeoff.iot.modbus.common.data.MiiHeartBeatData;

import lombok.Getter;


@Getter
public class HeartBeatData extends ReceiveDataEvent {

	private String deviceGroup;

	public HeartBeatData(Object source, int command, MiiHeartBeatData data) {
		super(source, command, data.device(), data.shelf(), data.slot());
		this.deviceGroup = data.deviceGroup();
	}

}
