package com.takeoff.iot.modbus.netty.handle.ais;

import com.takeoff.iot.modbus.netty.handle.MessageQ;
import com.takeoff.iot.modbus.netty.handle.UdpServerHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

@Slf4j
public class AisUdpSenderHandler extends UdpServerHandler {

	public AisUdpSenderHandler(MessageQ<String> messageQ, MessageQ<String> messageSendQ){
		super(messageQ,messageSendQ,null);
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws IOException {

	}
}
