package com.takeoff.iot.modbus.server;

import com.takeoff.iot.modbus.netty.handle.UdpServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

@Slf4j
public class MiiUdpSender extends MiiUdpServer{

    private Channel channel;
    public MiiUdpSender(UdpServerHandler senderHandler) throws IOException {
        super(getFreePort(),senderHandler);
    }

    public static Integer getFreePort() throws IOException {
        ServerSocket serverSocket =  new ServerSocket(0);
        int port = serverSocket.getLocalPort();
        log.info("获取空闲端口："+port);
        serverSocket.close();
        return port;
    }

    public boolean send(String ip,Integer port,String msg) throws InterruptedException {
        channel.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8),
                new InetSocketAddress(ip, port)
        )).sync();
        return true;
    }


        @Override
    public void start() throws InterruptedException {
        workerGroup = new NioEventLoopGroup();
        Bootstrap serverBootstrap = new Bootstrap();
        serverBootstrap
                .group(workerGroup)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .handler(handler);
        channel = serverBootstrap.bind(port).sync().channel();
        log.info("开启UDP 发送端口："+port);
    }

    @Override
    public void stop() {
        try {
            if (workerGroup != null)
                workerGroup.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
