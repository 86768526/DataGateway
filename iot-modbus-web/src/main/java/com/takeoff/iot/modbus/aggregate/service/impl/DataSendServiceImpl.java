package com.takeoff.iot.modbus.aggregate.service.impl;

import com.takeoff.iot.modbus.aggregate.service.DataSendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
@Slf4j
@Service
public class DataSendServiceImpl implements DataSendService {

    @Override
    public boolean send(String ip, Integer port, String content, Object sender) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method sendMethod = sender.getClass().getMethod("send",new Class[]{String.class,Integer.class,String.class});
        sendMethod.setAccessible(true);
        sendMethod.invoke(sender,new Object[]{ip,port,content});
        return true;
    }
}
