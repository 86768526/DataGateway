package com.takeoff.iot.modbus.netty.handle.gps;

import com.takeoff.iot.modbus.common.message.MiiMessage;
import com.takeoff.iot.modbus.netty.handle.MessageQ;
import com.takeoff.iot.modbus.netty.handle.TcpServerHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;

@Slf4j
@Sharable
public class GpsTcpServerHandler extends TcpServerHandler {

	public GpsTcpServerHandler(MessageQ<String> messageQ, MessageQ messageSendQ  ){
		super(messageQ,messageSendQ);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, MiiMessage msg) throws Exception {
//		 this.messageQ.add(message);
		final String[] message = new String[1];
		String data = new  String(msg.data().toBytes(),"GBK");
		GPSInfo  gpsInfo = GPSAnalysis.GNRMCAnalysis(data);
		if(null == gpsInfo){
			throw new Exception(" GPS数据解析失败，请检查数据格式");
		}
		log.info("TCP RECEIVE GPS DATA:"+msg.data());
		synchronized (this.messageQ) {
			this.messageQ.add("TCP RECEIVE GPS DATA:" + msg.data());
			this.messageQ.setNewDataFlag(true);
		}
		synchronized (this.messageSendQ) {
			this.messageSendQ.add(message[0]);
			this.messageSendQ.setNewDataFlag(true);
		}
		log.info(String.valueOf(this.messageQ.size()));
	}
}
