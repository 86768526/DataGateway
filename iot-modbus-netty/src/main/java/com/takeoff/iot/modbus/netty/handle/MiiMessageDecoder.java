package com.takeoff.iot.modbus.netty.handle;

import java.util.List;
import com.takeoff.iot.modbus.common.bytes.factory.MiiDataFactory;
import com.takeoff.iot.modbus.netty.channel.MiiChannel;
import com.takeoff.iot.modbus.netty.message.MiiInMessage;
import com.takeoff.iot.modbus.netty.message.NmeaInMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MiiMessageDecoder extends ByteToMessageDecoder {
	
	private MiiChannel channel;
	private MiiDataFactory dataFactory;

	public MiiMessageDecoder(MiiChannel channel, MiiDataFactory dataFactory) {
		this.channel = channel;
		this.dataFactory = dataFactory;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		try {
			byte[] array = new byte[in.readableBytes()];
			in.readBytes(array);
			NmeaInMessage msg = new NmeaInMessage(channel.name(),array,dataFactory);
			out.add(msg);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}
}
