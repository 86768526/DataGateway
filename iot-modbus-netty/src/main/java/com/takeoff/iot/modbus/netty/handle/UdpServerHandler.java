package com.takeoff.iot.modbus.netty.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Queue;

@Slf4j
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket>{

	public MessageQ<String> messageQ ;

	public MessageQ<String> messageSendQ;

	public UdpServerHandler(MessageQ<String> messageQ, MessageQ<String> messageSendQ){
		this.messageQ = messageQ;
		this.messageSendQ = messageSendQ;
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws IOException {

	}
}
