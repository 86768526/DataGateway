package com.takeoff.iot.modbus.aggregate.service;

import com.takeoff.iot.modbus.server.MiiServer;

import java.lang.reflect.InvocationTargetException;

public interface DataCollectService {

    public void startServer(Object miiServer) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;

    public void stopServer(Object miiServer) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;
}
