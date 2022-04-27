package com.takeoff.iot.modbus.netty.data.factory;

import com.takeoff.iot.modbus.common.bytes.factory.MiiDataFactory;
import com.takeoff.iot.modbus.common.data.*;
import com.takeoff.iot.modbus.common.message.MiiMessage;
import com.takeoff.iot.modbus.netty.data.*;
import com.takeoff.iot.modbus.netty.message.NmeaInMessage;


public class MiiServerDataFactory implements MiiDataFactory {

	@Override
	public MiiData buildData(int command, byte[] datas) {
		MiiData data = null;
		switch (command) {
			case NmeaInMessage.NMEA:
				data = new NmeaData(datas);
				break;
			case MiiMessage.HEARTBEAT:
			data = new MiiHeartBeatData(datas);
			break;
		case MiiMessage.LOCK:
			data = new MiiLockData(datas);
			break;
		case MiiMessage.CARD:
			data = new MiiCardData(datas);
			break;
		case MiiMessage.BARCODE:
			data = new MiiBarcodeData(datas);
			break;
		case MiiMessage.BACKLIGHT:
			data = new MiiBackLightData(datas);
			break;
		case MiiMessage.FINGER:
			data = new MiiFingerData(datas);
			break;
		case MiiMessage.HM:
			data = new MiiHumitureData(datas);
			break;
		case MiiMessage.LED:
		default: data = new MiiInData(datas);
			break;
		}
		return data;
	}
}
