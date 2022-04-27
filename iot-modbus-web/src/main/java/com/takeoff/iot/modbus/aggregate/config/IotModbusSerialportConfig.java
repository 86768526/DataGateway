package com.takeoff.iot.modbus.aggregate.config;

import com.takeoff.iot.modbus.serialport.service.SerialportService;
import com.takeoff.iot.modbus.aggregate.properties.IotModbusSerialportProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;


@Slf4j
@Configuration
public class IotModbusSerialportConfig implements ApplicationRunner {

    @Resource
    private IotModbusSerialportProperties iotModbusSerialportProperties;

    @Resource
    private SerialportService serialportService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(iotModbusSerialportProperties.getOpen()){
            if(iotModbusSerialportProperties.getNetty()){
                serialportService.openComPort(iotModbusSerialportProperties.getPort(), iotModbusSerialportProperties.getBaudrate(), iotModbusSerialportProperties.getThread());
            }else{
                serialportService.openComPort(iotModbusSerialportProperties.getPort(), iotModbusSerialportProperties.getBaudrate(), iotModbusSerialportProperties.getTimeout(), iotModbusSerialportProperties.getThread());
            }
        }
    }
}
