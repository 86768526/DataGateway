package com.takeoff.iot.modbus.serialport.data;

import com.takeoff.iot.modbus.common.data.MiiHumitureData;

import lombok.Getter;


@Getter
public class HumitureData extends ReceiveDataEvent{

    /**
     * 温度
     */
    private double temperature;

    /**
     * 湿度
     */
    private double humidity;

    public HumitureData(Object source, int command, MiiHumitureData data) {
        super(source, command, data.device(), data.shelf(), data.slot());
        this.temperature = data.temperature();
        this.humidity = data.humidity();
    }
}
