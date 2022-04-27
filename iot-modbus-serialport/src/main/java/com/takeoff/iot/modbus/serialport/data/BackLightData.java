package com.takeoff.iot.modbus.serialport.data;

import com.takeoff.iot.modbus.common.data.MiiBackLightData;

import lombok.Getter;


@Getter
public class BackLightData extends ReceiveDataEvent {

    private Integer statusCode;

    public BackLightData(Object source, Integer command, MiiBackLightData data) {
        super(source, command, data.device(), data.shelf(), data.slot());
        this.statusCode = data.statusCode();
    }
}
