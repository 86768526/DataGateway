package com.takeoff.iot.modbus.netty.handle;

import com.takeoff.iot.modbus.common.message.MiiMessage;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@Sharable
public class TcpServerHandler extends SimpleChannelInboundHandler<MiiMessage> {
	
//	private Map<Integer, MiiListener> listeners = new ConcurrentHashMap<>();
//	private MiiControlCentre centre;
    public MessageQ<String> messageQ ;
	public MessageQ<String> messageSendQ;
	public TcpServerHandler(/**MiiControlCentre centre, **/MessageQ<String> messageQ ,MessageQ<String> messageSendQ){
//		this.centre = centre;
		this.messageQ = messageQ;
		this.messageSendQ = messageSendQ;
	}


//	public MiiListener addListener(int command, MiiListener listener){
//		MiiListener pre = null;
//		if(hasListener(command)){
//			pre = listeners.get(command);
//		}
//		listeners.put(command, listener);
//		return pre;
//	}
	
//	public MiiListener removeListener(int command){
//		return listeners.remove(command);
//	}
//
//	public boolean hasListener(int command){
//		return listeners.get(command) != null;
//	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, MiiMessage msg) throws Exception {

	}
}
