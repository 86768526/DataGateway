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

	public AisTcpServerHandler(MessageQ<String> messageQ, MessageQ messageSendQ ){
		super(messageQ,messageSendQ);
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
		System.out.println(message[0]);
		synchronized (this.messageQ) {
			this.messageQ.add("TCP RECEIVE: ---" + msg.data());
			this.messageQ.setNewDataFlag(true);
		}
		synchronized ((this.messageSendQ)) {
			this.messageSendQ.add(message[0]);
			System.out.println(this.messageQ.size());
		}
//		MiiListener listener = listeners.get(msg.command());
//		if(listener != null){
//			MiiChannel channel = centre == null ? new MiiContextChannel(ctx) : centre.get(msg.deviceGroup());
//			listener.receive(channel == null ? new MiiContextChannel(ctx) : channel, msg);
//		}
	}
}
