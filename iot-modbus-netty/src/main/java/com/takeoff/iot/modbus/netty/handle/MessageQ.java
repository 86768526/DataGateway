package com.takeoff.iot.modbus.netty.handle;

import java.util.LinkedList;
public class MessageQ<E> extends LinkedList<E> {

    private static final long serialVersionUID = 1L;
    private final int size;
    private boolean newDataFlag;
    public MessageQ(int size) {
        this.size = size;
    }


    public boolean isNewDataFlag() {
        return newDataFlag;
    }

    public void setNewDataFlag(boolean newDataFlag) {
        this.newDataFlag = newDataFlag;
    }

    @Override
    public boolean add(E o) {
        super.add(o);
        while (size() > size) {
            super.remove();
        }
        return true;
    }

}
