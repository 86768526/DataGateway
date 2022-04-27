package com.takeoff.iot.modbus.serialport.data;

import java.util.List;

import com.takeoff.iot.modbus.common.data.MiiFingerData;

import lombok.Getter;


@Getter
public class FingerData extends ReceiveDataEvent {

    private int fingerType;
    private int fingerCmd;
    private int fingerId;
    private List fingerIdList;
    private byte[] fingerTemplate;

    public FingerData(Object source, int command, MiiFingerData data) {
        super(source, command, data.device(), data.shelf(), data.slot());
        this.fingerType = data.fingerType();
        this.fingerCmd = data.fingerCmd();
        this.fingerId = data.fingerId();
        this.fingerIdList = data.fingerIdList();
        this.fingerTemplate = data.fingerTemplate();
    }

}
