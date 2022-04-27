package com.takeoff.iot.modbus.server;
import com.takeoff.iot.modbus.netty.channel.MiiChannelGroup;
import com.takeoff.iot.modbus.netty.handle.UdpServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;

@Slf4j
public class MiiUdpServer {

	private static final int IDLE_TIMEOUT = 60;
	protected EventLoopGroup workerGroup;
	private ChannelFuture future;
	protected int port;
	private String address;
	public Queue<String> messageQ;
	@Getter
	private MiiChannelGroup groups;
	protected UdpServerHandler handler;

	public MiiUdpServer(int port,UdpServerHandler handler){
		this("",port, 0,handler);
	}

	public MiiUdpServer(String  address, int port,UdpServerHandler handler){
		this(address,port, 0,handler);
	}
	/**
	 * 创建指定服务端口，指定线程数的服务端
	 * @param port 服务端口
	 * @param nThread 执行线程池线程数
	 */
	public MiiUdpServer(String address, int port, int nThread, UdpServerHandler handler){
		this.port = port;
		this.address = address;
		this.groups = new MiiChannelGroup();
		this.messageQ = handler.messageQ;
		this.handler = handler;
	}

	/**
	 * 启动服务
	 */
	public void start() throws InterruptedException {
		workerGroup = new NioEventLoopGroup();
		Bootstrap serverBootstrap = new Bootstrap();
		serverBootstrap
				.group(workerGroup)
				.channel(NioDatagramChannel.class)
				.option(ChannelOption.SO_BROADCAST, true)
				.option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
				.handler(this.handler);
		future = serverBootstrap.bind(address,port);
		log.info("开启UDP端口："+port);
	}
	/**
	 * 停止服务
	 */
	public void stop(){
		future.channel().closeFuture();
		workerGroup.shutdownGracefully();
	}

}
