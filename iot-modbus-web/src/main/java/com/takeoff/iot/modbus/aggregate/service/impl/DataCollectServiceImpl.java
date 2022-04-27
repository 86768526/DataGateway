package com.takeoff.iot.modbus.aggregate.service.impl;
import com.takeoff.iot.modbus.aggregate.service.DataCollectService;
import com.takeoff.iot.modbus.server.MiiServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@Service
public class DataCollectServiceImpl implements DataCollectService {

    @Override
    public void startServer(Object miiServer) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method startmethod = miiServer.getClass().getMethod("start",null);
        startmethod.invoke(miiServer);
    }

    @Override
    public void stopServer(Object miiServer) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method startmethod = miiServer.getClass().getMethod("stop",null);
        startmethod.invoke(miiServer);    }
}
