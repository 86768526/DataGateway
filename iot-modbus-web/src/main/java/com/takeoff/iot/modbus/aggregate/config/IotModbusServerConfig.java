package com.takeoff.iot.modbus.aggregate.config;
import com.takeoff.iot.modbus.aggregate.listener.*;
import com.takeoff.iot.modbus.common.message.MiiMessage;
import com.takeoff.iot.modbus.server.MiiServer;
import com.takeoff.iot.modbus.aggregate.properties.IotModbusServerProperties;
import com.takeoff.iot.modbus.server.MiiUdpServer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
@Configuration
public class IotModbusServerConfig implements ApplicationRunner {

	@Resource
	private IotModbusServerProperties iotModbusServerProperties;

	@Resource
	private CardListener cardListener;

	@Resource
	private BarCodeListener barCodeListener;

	@Resource
	private BackLightListener backLightListener;

	@Resource
	private LockListener lockListener;

	@Resource
	private FingerListener fingerListener;

	@Resource
	private HumitureListener humitureListener;

	@Resource
	private NmeaDataListener nmeaDataListener;
	
	@Getter
	private MiiServer miiServer;

	@Override
    public void run(ApplicationArguments args) throws Exception {
		if(iotModbusServerProperties.getOpen()){
			miiServer = new MiiServer(iotModbusServerProperties.getPort(), iotModbusServerProperties.getThread());
			miiServer.addListener(MiiMessage.BACKLIGHT, backLightListener);
			miiServer.addListener(MiiMessage.LOCK, lockListener);
			miiServer.addListener(MiiMessage.CARD, cardListener);
			miiServer.addListener(MiiMessage.BARCODE, barCodeListener);
			miiServer.addListener(MiiMessage.FINGER, fingerListener);
			miiServer.addListener(MiiMessage.HM, humitureListener);
			miiServer.addListener(MiiMessage.NMEA, nmeaDataListener);
			//log.info("IOT通讯协议已开启Socket服务，占用端口： " + iotModbusServerProperties.getPort() + ",执行线程池线程数:" + iotModbusServerProperties.getThread());
			miiServer.start();
			MiiUdpServer udpServer = new MiiUdpServer("0.0.0.0",4001);
			udpServer.start();
		}else{
			log.info("IOT通讯协议未开启Socket服务");
		}
	}
}
