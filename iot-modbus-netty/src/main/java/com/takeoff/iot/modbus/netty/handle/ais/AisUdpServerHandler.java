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

//	private String unCompletePacket = "";
//	private String lastSource = "";
	public AisUdpServerHandler(MessageQ<String> SourceMessageQ , MessageQ messageSendQ,MessageQ<String> httpMessageQ){
		super(SourceMessageQ,messageSendQ,httpMessageQ);
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
		if(null == message[0]){
			this.messageQ.add("UDP RECEIVE UNRESOLVE PACKET: ******" + sourceStr.replaceAll("\n",""));
			this.messageQ.setNewDataFlag(true);
			return;
		}
		log.info(message[0]);
		synchronized (this.messageQ) {
			this.messageQ.add("UDP RECEIVE: ---" + sourceStr.replaceAll("\n",""));
			this.messageQ.setNewDataFlag(true);
		}
		synchronized ((this.UdpMessageSendQ)) {
			this.UdpMessageSendQ.add(message[0]);
			this.UdpMessageSendQ.setNewDataFlag(true);
		}
		synchronized (this.httpMessageSendQ){
			this.httpMessageSendQ.add(message[0]);
			this.httpMessageSendQ.setNewDataFlag(true);
		}
		log.info(String.valueOf(this.messageQ.size()));
	}
}
