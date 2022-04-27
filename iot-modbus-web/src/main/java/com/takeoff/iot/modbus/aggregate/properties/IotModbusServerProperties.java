package com.takeoff.iot.modbus.aggregate.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;


@Data
@Component
@ConfigurationProperties(prefix = "iot.netty.server")
public class IotModbusServerProperties {
	
	/**
	 * 是否开启Socket服务
	 */
	private Boolean open;

	/**
	 * Socket服务端口
	 */
	private Integer port;
	
	/**
	 * Socket服务执行线程数
	 */
    private Integer thread;

    private Integer udpPort;
}
