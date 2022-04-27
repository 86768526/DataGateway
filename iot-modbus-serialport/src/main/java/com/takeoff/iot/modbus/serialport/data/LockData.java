package com.takeoff.iot.modbus.serialport.data;

import java.util.List;

import com.takeoff.iot.modbus.common.data.MiiLockData;
import com.takeoff.iot.modbus.common.entity.LockStatus;

import lombok.Getter;


@Getter
public class LockData extends ReceiveDataEvent {

    /**
     * 锁状态
     */
    private List<LockStatus> list;

    public LockData(Object source, int command, MiiLockData data) {
        super(source, command, data.device(), data.shelf(), data.slot());
        this.list = data.list();
    }

}
