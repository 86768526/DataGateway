package com.takeoff.iot.modbus.netty.handle.gps;

import com.takeoff.iot.modbus.netty.handle.MessageQ;
import com.takeoff.iot.modbus.netty.handle.UdpServerHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Queue;

@Slf4j
public class GpsUdpServerHandler extends UdpServerHandler {

	public GpsUdpServerHandler(MessageQ<String> messageQ, MessageQ<String> messageSendQ){
		super(messageQ,messageSendQ);
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws IOException {
		String data = packet.content().toString(Charset.forName("GBK"));
		GPSInfo  gpsInfo = GPSAnalysis.GNRMCAnalysis(data);
		log.info("UDP RECEIVE GPS DATA:"+gpsInfo.toString());
		this.messageQ.add("UDP RECEIVE GPS DATA"+data);
		this.messageSendQ.add(gpsInfo.toString());
		log.info(String.valueOf(this.messageQ.size()));
	}
}
