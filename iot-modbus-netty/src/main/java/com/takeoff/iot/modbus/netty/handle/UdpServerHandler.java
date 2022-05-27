package com.takeoff.iot.modbus.netty.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

@Slf4j
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket>{

	public MessageQ<String> messageQ ;

	public MessageQ<String> UdpMessageSendQ;

	public MessageQ<String> httpMessageSendQ;

	public UdpServerHandler(MessageQ<String> messageQ, MessageQ<String> messageSendQ,MessageQ<String> httpMessageSendQ){
		this.messageQ = messageQ;
		this.UdpMessageSendQ = messageSendQ;
		this.httpMessageSendQ = httpMessageSendQ;
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws IOException {

	}
}
