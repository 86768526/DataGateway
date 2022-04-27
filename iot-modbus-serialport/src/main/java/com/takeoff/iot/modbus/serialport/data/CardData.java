package com.takeoff.iot.modbus.serialport.data;

import com.takeoff.iot.modbus.common.data.MiiCardData;

import lombok.Getter;


@Getter
public class CardData extends ReceiveDataEvent {

	private String cardCode;

	public CardData(Object source, int command, MiiCardData data) {
		super(source, command, data.device(), data.shelf(), data.slot());
		this.cardCode = data.content();
	}

}
