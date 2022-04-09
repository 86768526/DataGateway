package com.takeoff.iot.modbus.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.takeoff.iot.modbus.netty.device.MiiDeviceChannel;
import com.takeoff.iot.modbus.netty.device.MiiDeviceGroup;
import com.takeoff.iot.modbus.netty.device.MiiControlCentre;
import com.takeoff.iot.modbus.common.bytes.factory.MiiDataFactory;
import com.takeoff.iot.modbus.common.data.MiiHeartBeatData;
import com.takeoff.iot.modbus.common.message.MiiMessage;
import com.takeoff.iot.modbus.netty.channel.MiiChannel;
import com.takeoff.iot.modbus.netty.channel.MiiChannelGroup;
import com.takeoff.iot.modbus.netty.data.factory.MiiServerDataFactory;
import com.takeoff.iot.modbus.netty.handle.*;
import com.takeoff.iot.modbus.netty.listener.MiiListener;
import com.takeoff.iot.modbus.netty.listener.UdpListener;
import com.takeoff.iot.modbus.server.message.sender.MiiServerMessageSender;
import com.takeoff.iot.modbus.server.message.sender.ServerMessageSender;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MiiUdpServer {

	private static final int IDLE_TIMEOUT = 60;
	private EventLoopGroup workerGroup;
	private ChannelFuture future;
	private int port;
	private String address;
	@Getter
	private MiiChannelGroup groups;
	private ServerMessageSender sender;
	private UdpServerHandler handler;
	private MiiDataFactory dataFactory;

	/**
	 * 创建指定服务端口，默认线程数的服务端
	 * @param port 服务端口
	 */
	public MiiUdpServer(int port){
		this("",port, 0);
	}

	public MiiUdpServer(String  address, int port){
		this(address,port, 0);
	}
	/**
	 * 创建指定服务端口，指定线程数的服务端
	 * @param port 服务端口
	 * @param nThread 执行线程池线程数
	 */
	public MiiUdpServer(String address,int port, int nThread){
		this.port = port;
		this.address = address;
		this.groups = new MiiChannelGroup();
//		this.sender = new MiiServerMessageSender(this.groups);
		this.handler = new UdpServerHandler();
//		this.handler.addListener(MiiMessage.HEARTBEAT, new MiiListener() {
//
//			@Override
//			public void receive(MiiChannel channel, MiiMessage message) {
//				MiiHeartBeatData data = (MiiHeartBeatData) message.data();
//				//通讯通道绑定设备组编码
//				groups.get(message.deviceGroup()).name(data.deviceGroup());
//				log.info("Netty通讯已绑定设备组编码："+data.deviceGroup());
//			}
//		});
		this.dataFactory = new MiiServerDataFactory();
	}

	/**
	 * 启动服务
	 */
	public void start(){
		workerGroup = new NioEventLoopGroup();
		Bootstrap serverBootstrap = new Bootstrap();
		serverBootstrap
				.group(workerGroup)
				.channel(NioDatagramChannel.class)
				.option(ChannelOption.SO_BROADCAST, true)
				.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
				.handler(new UdpServerHandler());
		future = serverBootstrap.bind(address,port);
	}
	/**
	 * 停止服务
	 */
	public void stop(){
		future.channel().closeFuture();
		workerGroup.shutdownGracefully();
	}
}
