package com.takeoff.iot.modbus.aggregate.service;

import java.lang.reflect.InvocationTargetException;

public interface DataSendService {
    boolean send(String ip, Integer port, String Content,Object sender) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;

    boolean httpSend(String url,String data);
}
