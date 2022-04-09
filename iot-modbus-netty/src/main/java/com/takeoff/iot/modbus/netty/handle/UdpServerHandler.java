package com.takeoff.iot.modbus.netty.handle;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket>{

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
		ByteBuf bufData = packet.content();
		String str = bufData.toString(Charset.forName("GBK"));
		System.out.println("服务端接收到消息：" + str);
	}
}
