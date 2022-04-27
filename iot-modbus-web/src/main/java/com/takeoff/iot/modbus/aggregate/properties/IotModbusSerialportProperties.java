package com.takeoff.iot.modbus.aggregate.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "iot.serialport")
public class IotModbusSerialportProperties {

    /**
     * 是否开启串口服务
     */
    private Boolean open;

    /**
     * 串口号
     */
    private String port;

    /**
     * 是否使用netty对数据进行拆包处理
     */
    private Boolean netty;

    /**
     * 链接超时时间，不使用netty对数据进行拆包处理时必填
     */
    private Integer timeout;

    /**
     * 波特率
     */
    private Integer baudrate;

    /**
     * 设置通讯服务执行线程数
     */
    private Integer thread;
}
