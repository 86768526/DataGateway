package com.takeoff.iot.modbus.netty.handle.ais;

import com.takeoff.iot.modbus.common.message.MiiMessage;
import com.takeoff.iot.modbus.netty.handle.MessageQ;
import com.takeoff.iot.modbus.netty.handle.TcpServerHandler;
import dk.tbsalling.aismessages.AISInputStreamReader;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Queue;


@Sharable
public class AisTcpServerHandler extends TcpServerHandler {

	public AisTcpServerHandler(MessageQ<String> SourceMessageQ, MessageQ udpMessageSendQ ,MessageQ<String> httpMessageQ){
		super(SourceMessageQ,udpMessageSendQ,httpMessageQ);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, MiiMessage msg) throws Exception {
//		 this.messageQ.add(message);
		final String[] message = new String[1];
		String data = "!"+new  String(msg.data().toBytes(),"GBK");
//		InputStream inputStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
		InputStream inputStream = new ByteArrayInputStream(msg.data().toBytes());
		AISInputStreamReader streamReader = new AISInputStreamReader(inputStream, aisMessage -> {
			message[0] = aisMessage.toString();
		});

		streamReader.run();
		if(null == message[0]){
			this.messageQ.add("TCP RECEIVE UNRESOLVE PACKET: ******" + msg.data());
			this.messageQ.setNewDataFlag(true);
			return;
		}
		System.out.println(message[0]);
		System.out.println(this.messageQ.size());
		synchronized (this.messageQ) {
			this.messageQ.add("TCP RECEIVE: ---" + msg.data());
			this.messageQ.setNewDataFlag(true);
		}
		synchronized (this.messageSendQ) {
			this.messageSendQ.add(message[0]);
			this.messageSendQ.setNewDataFlag(true);
		}
		synchronized (this.httpMessageSendQ) {
			this.httpMessageSendQ.add(message[0]);
			this.httpMessageSendQ.setNewDataFlag(true);
		}
//		MiiListener listener = listeners.get(msg.command());
//		if(listener != null){
//			MiiChannel channel = centre == null ? new MiiContextChannel(ctx) : centre.get(msg.deviceGroup());
//			listener.receive(channel == null ? new MiiContextChannel(ctx) : channel, msg);
//		}
	}
}
