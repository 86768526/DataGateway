package com.takeoff.iot.modbus.netty.handle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;

public class MiiBasedFrameDecoder extends LineBasedFrameDecoder {
	
	public MiiBasedFrameDecoder(){
//		super(Integer.MAX_VALUE,true,true,new ByteBuf[] {Unpooled.wrappedBuffer(new byte[]{'!'}),Unpooled.wrappedBuffer(new byte[]{'$'})});
		super(Integer.MAX_VALUE,true,true);
	}
}
