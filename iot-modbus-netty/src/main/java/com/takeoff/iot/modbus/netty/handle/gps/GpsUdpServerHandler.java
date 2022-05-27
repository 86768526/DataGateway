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

	public GpsUdpServerHandler(MessageQ<String> messageQ, MessageQ<String> messageSendQ,MessageQ<String> httpMessageQ){
		super(messageQ,messageSendQ,httpMessageQ);
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws IOException {
		String data = packet.content().toString(Charset.forName("GBK"));
		GPSInfo  gpsInfo = GPSAnalysis.GNRMCAnalysis(data);
		log.info("UDP RECEIVE GPS DATA:"+gpsInfo.toString());
		synchronized (this.messageQ) {
			this.messageQ.add("UDP RECEIVE GPS DATA" + data);
			this.messageQ.setNewDataFlag(true);
		}
		synchronized (this.UdpMessageSendQ) {
			this.UdpMessageSendQ.add(gpsInfo.toString());
			this.UdpMessageSendQ.setNewDataFlag(true);
		}
		synchronized (this.httpMessageSendQ){
			this.httpMessageSendQ.add(gpsInfo.toString());
			this.httpMessageSendQ.setNewDataFlag(true);
		}
		log.info(String.valueOf(this.messageQ.size()));
	}
}
