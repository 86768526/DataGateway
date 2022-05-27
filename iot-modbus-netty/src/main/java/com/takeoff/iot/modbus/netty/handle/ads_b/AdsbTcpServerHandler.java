package com.takeoff.iot.modbus.netty.handle.ads_b;

import com.takeoff.iot.modbus.common.message.MiiMessage;
import com.takeoff.iot.modbus.netty.handle.MessageQ;
import com.takeoff.iot.modbus.netty.handle.TcpServerHandler;
import com.takeoff.iot.modbus.netty.handle.gps.GPSAnalysis;
import com.takeoff.iot.modbus.netty.handle.gps.GPSInfo;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable
public class AdsbTcpServerHandler extends TcpServerHandler {

	public AdsbTcpServerHandler(MessageQ<String> messageQ, MessageQ messageSendQ , MessageQ<String> httpMessageQ ){
		super(messageQ,messageSendQ,httpMessageQ);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, MiiMessage msg) throws Exception {
//		 this.messageQ.add(message);
//		final String[] message = new String[1];
		String data = new  String(msg.data().toBytes(),"GBK");
		GPSInfo gpsInfo = GPSAnalysis.GNRMCAnalysis(data);
		if(null == gpsInfo){
			throw new Exception(" GPS数据解析失败，请检查数据格式");
		}
		log.info("TCP RECEIVE GPS DATA:"+msg.data());
		synchronized (this.messageQ) {
			this.messageQ.add("TCP RECEIVE GPS DATA:" + data);
			this.messageQ.setNewDataFlag(true);
		}
		synchronized (this.messageSendQ) {
			this.messageSendQ.add(gpsInfo.toString());
			this.messageSendQ.setNewDataFlag(true);
		}
		synchronized (this.httpMessageSendQ){
			this.httpMessageSendQ.add(gpsInfo.toString());
			this.httpMessageSendQ.setNewDataFlag(true);
		}
		log.info(String.valueOf(this.messageQ.size()));
	}
}
