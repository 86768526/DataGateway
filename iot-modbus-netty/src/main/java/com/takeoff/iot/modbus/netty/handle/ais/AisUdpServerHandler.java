package com.takeoff.iot.modbus.netty.handle.ais;

import com.takeoff.iot.modbus.netty.handle.MessageQ;
import com.takeoff.iot.modbus.netty.handle.UdpServerHandler;
import dk.tbsalling.aismessages.AISInputStreamReader;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@Slf4j
public class AisUdpServerHandler extends UdpServerHandler {

	public AisUdpServerHandler(MessageQ<String> messageQ , MessageQ messageSendQ){
		super(messageQ,messageSendQ);
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws IOException {
		final String[] message = new String[1];
		String sourceStr = packet.content().toString(Charset.forName("GBK"));
		InputStream inputStream = new ByteArrayInputStream(sourceStr.getBytes());
		AISInputStreamReader streamReader = new AISInputStreamReader(inputStream, aisMessage -> {
			message[0] = aisMessage.toString();
		});
		streamReader.run();
		log.info(message[0]);
		synchronized (this.messageQ) {
			this.messageQ.add("UDP RECEIVE: ---" + sourceStr);
			this.messageQ.setNewDataFlag(true);
		}
		synchronized ((this.messageSendQ)) {
			this.messageSendQ.add(message[0]);
			this.messageSendQ.setNewDataFlag(true);
		}
		log.info(String.valueOf(this.messageQ.size()));
	}
}
