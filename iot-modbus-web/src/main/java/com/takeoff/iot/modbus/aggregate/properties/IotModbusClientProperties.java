package com.takeoff.iot.modbus.aggregate.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "iot.netty.client")
public class IotModbusClientProperties {

    /**
     * 是否开启Socket服务
     */
    private Boolean open;

    /**
     * 服务IP
     */
    private String ip;

    /**
     * 服务端口
     */
    private Integer port;

    /**
     * Socket服务执行线程数
     */
    private Integer thread;

    /**
     * 设备组编码
     */
    private String deviceGroup;
}
