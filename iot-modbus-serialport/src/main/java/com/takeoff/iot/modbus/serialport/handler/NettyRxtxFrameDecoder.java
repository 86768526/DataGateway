package com.takeoff.iot.modbus.serialport.handler;

import java.nio.ByteOrder;

import com.takeoff.iot.modbus.common.message.MiiMessage;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


public class NettyRxtxFrameDecoder extends LengthFieldBasedFrameDecoder {

	public NettyRxtxFrameDecoder(){
		super(ByteOrder.LITTLE_ENDIAN, Integer.MAX_VALUE, MiiMessage.DATA_INDEX, MiiMessage.DATA_SIZE, 3, 0, true);
	}
}
