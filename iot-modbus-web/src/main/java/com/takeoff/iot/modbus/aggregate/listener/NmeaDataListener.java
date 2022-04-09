package com.takeoff.iot.modbus.aggregate.listener;

import com.takeoff.iot.modbus.common.data.MiiHumitureData;
import com.takeoff.iot.modbus.common.data.NmeaData;
import com.takeoff.iot.modbus.common.message.MiiMessage;
import com.takeoff.iot.modbus.netty.channel.MiiChannel;
import com.takeoff.iot.modbus.netty.listener.MiiListener;
import com.takeoff.iot.modbus.serialport.data.HumitureData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 类功能说明：温湿度指令监听器<br/>
 * 公司名称：TF（腾飞）开源 <br/>
 * 作者：luorongxi <br/>
 */
@Slf4j
@Component
public class NmeaDataListener implements MiiListener {

	@Override
	public void receive(MiiChannel channel, MiiMessage message) {
		if(message.command() == MiiMessage.NMEA){
			NmeaData data = (NmeaData) message.data();
			String NmeaStr = "";
			try {
				NmeaStr = new String(data.toBytes(), "GBK");
			}catch (IOException e){
				log.info("解析字节流出错:"+e.getMessage());
				e.printStackTrace();
			}
			log.info(message.command() +"接收到NMEA协议tcp数据: "+NmeaStr);
        }
	}

	@EventListener
	public void handleReceiveDataEvent(NmeaData data) {
		log.info("接收到NMEA协议TCP数据: "+ data.command());
	}
}
