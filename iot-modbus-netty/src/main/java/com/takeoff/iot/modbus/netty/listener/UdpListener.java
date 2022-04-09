package com.takeoff.iot.modbus.netty.listener;

import com.takeoff.iot.modbus.common.message.MiiMessage;
import com.takeoff.iot.modbus.netty.channel.MiiChannel;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;

@FunctionalInterface
public interface UdpListener {
	
	/**
	 * 处理接收到的设备信息
	 * @param channel 消息通道
	 * @param message 接收到的设备信息
	 */
	void receive(MiiChannel channel, DatagramPacket message);
}
