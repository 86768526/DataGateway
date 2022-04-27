package com.takeoff.iot.modbus.serialport.data;

import com.takeoff.iot.modbus.common.data.MiiBarcodeData;

import lombok.Getter;


@Getter
public class BarCodeData extends ReceiveDataEvent {
	
	private String barCode;

	public BarCodeData(Object source, int command, MiiBarcodeData data) {
		super(source, command, data.device(), data.shelf(), data.slot());
		this.barCode = data.content();
	}

}
