package com.takeoff.iot.modbus.netty.handle.ads_b;

import cn.hutool.json.JSONUtil;
import com.takeoff.iot.modbus.netty.handle.MessageQ;
import com.takeoff.iot.modbus.netty.handle.UdpServerHandler;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.opensky.libadsb.Position;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AdsbUdpServerHandler extends UdpServerHandler {

	public AdsbUdpServerHandler(MessageQ<String> messageQ, MessageQ<String> messageSendQ,MessageQ<String> httpMessageQ){
		super(messageQ,messageSendQ,httpMessageQ);
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws IOException {
		AdsbProcess dec = new AdsbProcess();
		Position rec = new Position(112.7103, 28.1556, 23.5);
		String value = ByteBufUtil.hexDump(packet.content());
		String[] adbsData = new String[1];
		String row = dec.subStrRowData(adbsData,value);
		Map result = new HashMap();
		int count = 0;
		while(null!=row&&!"".equals(row)){
			count ++;
			dec.decodeMsg(new Date().getTime(),
					adbsData[0],
					rec,
					result
			);
			row = dec.subStrRowData(adbsData,row);
			String message=  JSONUtil.toJsonStr(result);
			synchronized (this.messageQ) {
				this.messageQ.add("UDP RECEIVE: ---" + message);
				this.messageQ.setNewDataFlag(true);
			}
			synchronized ((this.UdpMessageSendQ)) {
				this.UdpMessageSendQ.add(message);
				this.UdpMessageSendQ.setNewDataFlag(true);
			}
			synchronized (this.httpMessageSendQ){
				this.httpMessageSendQ.add(message);
				this.httpMessageSendQ.setNewDataFlag(true);
			}
		}
//		System.out.println(count);
	}
}
