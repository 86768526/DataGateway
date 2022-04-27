package com.takeoff.iot.modbus.netty.handle;

import org.bouncycastle.util.encoders.Hex;

import com.takeoff.iot.modbus.common.message.MiiMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MiiMessageEncoder extends MessageToByteEncoder<MiiMessage> {

	@Override
	public void encode(ChannelHandlerContext ctx, MiiMessage msg, ByteBuf out) throws Exception {
		try {
			out.writeBytes(msg.toBytes());
			log.debug(Hex.toHexString(msg.toBytes()));
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

}
