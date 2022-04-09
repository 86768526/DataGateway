package com.takeoff.iot.modbus.netty.handle;

import com.takeoff.iot.modbus.common.bytes.factory.MiiDataFactory;
import com.takeoff.iot.modbus.common.data.MiiData;
import com.takeoff.iot.modbus.common.data.NmeaData;
import com.takeoff.iot.modbus.common.message.MiiMessage;
import com.takeoff.iot.modbus.netty.channel.MiiChannel;
import com.takeoff.iot.modbus.netty.channel.MiiContextChannel;
import com.takeoff.iot.modbus.netty.device.MiiControlCentre;
import com.takeoff.iot.modbus.netty.listener.MiiListener;
import com.takeoff.iot.modbus.netty.listener.UdpListener;
import com.takeoff.iot.modbus.netty.message.NmeaInMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket>{
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws UnsupportedEncodingException {
		ByteBuf bufData = packet.content();
		String str = bufData.toString(Charset.forName("GBK"));
	}
}
