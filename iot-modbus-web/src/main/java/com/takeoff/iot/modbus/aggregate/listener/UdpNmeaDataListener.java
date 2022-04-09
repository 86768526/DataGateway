package com.takeoff.iot.modbus.aggregate.listener;

import com.takeoff.iot.modbus.common.data.NmeaData;
import com.takeoff.iot.modbus.common.message.MiiMessage;
import com.takeoff.iot.modbus.netty.channel.MiiChannel;
import com.takeoff.iot.modbus.netty.listener.MiiListener;
import com.takeoff.iot.modbus.netty.listener.UdpListener;
import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
@Slf4j
@Component
public class UdpNmeaDataListener implements UdpListener {

	@Override
	public void receive(MiiChannel channel, DatagramPacket message) {
			String NmeaStr = "";
				ByteBuf bufData = message.content();
				NmeaStr = bufData.toString(Charset.forName("GBK"));
			log.info(998 +"接收到NMEA协议tcp数据: "+NmeaStr);
	}

	@EventListener
	public void handleReceiveDataEvent(NmeaData data) {
		log.info("接收到NMEA协议UDP数据: "+ data.command());
	}
}
