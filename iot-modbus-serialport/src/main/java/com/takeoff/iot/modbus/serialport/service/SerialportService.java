package com.takeoff.iot.modbus.serialport.service;


public interface SerialportService {

    /**
     * 连接串口
     * @param port
     * @param baudrate
     * @param timeout
     * @param thread
     */
    void openComPort(String port, Integer baudrate, Integer timeout, Integer thread);

    /**
     * netty连接串口
     * @param port
     * @param baudrate
     * @param thread
     */
    void openComPort(String port, Integer baudrate, Integer thread);

    /**
     * 关闭串口
     */
    void closeSerialPort();

    /**
     * 发送数据到串口
     * @param bytes
     */
    void serialportSendData(byte[] bytes);

}
