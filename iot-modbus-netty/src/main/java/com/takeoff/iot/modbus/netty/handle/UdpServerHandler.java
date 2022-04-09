package com.takeoff.iot.modbus.netty.handle;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.DatagramPacket;

@Slf4j
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket>{

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
		String str = "";
		try {
			 str = new String(packet.getData(), "GBK");
		}catch (IOException e){
			e.printStackTrace();
		}
		System.out.println("服务端接收到消息：" + str);
	}
}
