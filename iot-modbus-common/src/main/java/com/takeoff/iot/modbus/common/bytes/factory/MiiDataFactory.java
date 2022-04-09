package com.takeoff.iot.modbus.common.bytes.factory;

import com.takeoff.iot.modbus.common.data.MiiData;
import com.takeoff.iot.modbus.common.data.NmeaData;

public interface MiiDataFactory {
	MiiData buildData(int command, byte[] datas);
}
