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

    public static void main(String args[]){
        MessageQ<String> m  = new MessageQ<String>(5);
        for(int i=0;i<5;i++){
            m.add("fervgfgb"+i);
        }
        for(int i=0;i<m.size;i++){
            System.out.println(m.remove());
        }
        System.out.println(m);
    }
}
