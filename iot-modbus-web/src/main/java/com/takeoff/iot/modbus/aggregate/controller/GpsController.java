package com.takeoff.iot.modbus.aggregate.controller;
import cn.hutool.core.net.NetUtil;
import com.google.common.collect.Maps;
import com.takeoff.iot.modbus.aggregate.service.DataCollectService;
import com.takeoff.iot.modbus.aggregate.service.DataSendService;
import com.takeoff.iot.modbus.aggregate.utils.R;
import com.takeoff.iot.modbus.netty.handle.MessageQ;
import com.takeoff.iot.modbus.netty.handle.TcpServerHandler;
import com.takeoff.iot.modbus.netty.handle.UdpServerHandler;
import com.takeoff.iot.modbus.netty.handle.ais.AisUdpSenderHandler;
import com.takeoff.iot.modbus.netty.handle.gps.GpsTcpServerHandler;
import com.takeoff.iot.modbus.netty.handle.gps.GpsUdpServerHandler;
import com.takeoff.iot.modbus.server.MiiServer;
import com.takeoff.iot.modbus.server.MiiUdpSender;
import com.takeoff.iot.modbus.server.MiiUdpServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/gps")
public class GpsController {

        @Resource
        private DataCollectService dataCollectService;

        @Resource
        private DataSendService sendService;
        public static Map<Integer, Object> startedServer = Maps.newHashMap();
        public static Map<String, Object> startedUdpTransfer = Maps.newHashMap();
        public static MessageQ<String> UdpMessageSendQ = new MessageQ<>(10);
        public static MessageQ<String> UdpMessageSendedQ = new MessageQ<>(10);
        public static MessageQ<String> HttpMessageSendQ = new MessageQ<>(10);
        public static MessageQ<String> HttpMessageSendedQ = new MessageQ<>(10);
        public static boolean ifShutDownHttpTransfer = false;

    @RequestMapping("/stopHttpTransfer")
    @ResponseBody
    public R stopHttpTransfer(){
        ifShutDownHttpTransfer = true;
        return R.ok("关闭HTTP转发");
    }


    @RequestMapping("/startHttpTransfer")
    @ResponseBody
    public R startHttpTransfer(@RequestParam(value = "httpSendFq") Integer fq,
                               @RequestParam(value = "httpurl")String url,
                               @RequestParam(value = "device_id")String device_id){

        ifShutDownHttpTransfer = false;
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @SneakyThrows
            public void run(){
                if(ifShutDownHttpTransfer){
                    timer.cancel();
                }
                synchronized (HttpMessageSendQ) {
                    for (int i = HttpMessageSendQ.size(); i > 0; i--) {
                        String message = HttpMessageSendQ.remove();
                        sendService.httpSend(url,
                                message + ",deviceCode=" + device_id + ",protocal=GP"
                        );
                        synchronized (HttpMessageSendedQ) {
                            HttpMessageSendedQ.add("send to(" + url + "):" + message);
                            HttpMessageSendedQ.setNewDataFlag(true);
                        }
                    }
                    HttpMessageSendQ.setNewDataFlag(false);
                }
            }
        },fq*1000 , fq*1000);
        return R.ok("ok");
    }

    @RequestMapping(value = "/HttpTransferEventSourceServer",
            produces="text/event-stream;charset=UTF-8")
    public void HttpTransferEventSourceServer(HttpServletRequest request,
                                              HttpServletResponse response) throws InterruptedException, IOException {
        ifShutDownHttpTransfer = false;
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @SneakyThrows
            public void run(){
                if(ifShutDownHttpTransfer){
                    timer.cancel();
                }
                response.setContentType("text/event-stream");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(200);
                response.getWriter().write("data:heartbeat\n\n");
                response.getWriter().flush();
            }
        },30000 , 30000);
        String data ="";
        while(!ifShutDownHttpTransfer){
            TimeUnit.MICROSECONDS.sleep(10);
            if(HttpMessageSendedQ.isNewDataFlag()) {
                response.setContentType("text/event-stream");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(200);
                synchronized (HttpMessageSendedQ) {
                    for (int i = HttpMessageSendedQ.size(); i > 0; i--) {
                        data = HttpMessageSendedQ.remove();
                        response.getWriter().write("data:" + data + "\n\n");
                        response.getWriter().flush();
                    }
                    HttpMessageSendedQ.setNewDataFlag(false);
                  }
                }
            }
        response.getWriter().close();
    }

    /**
     * 开启UDP 转发
     * @param udpsendfq
     * @param unicast
     * @param muticast
     * @param ip
     * @param port
     * @return
     * @throws IOException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     */
    private static boolean ifUdpTransferToStop = false;
    @RequestMapping(value = "/startUdpTransfer")
    @ResponseBody
    public R startUdpTransfer(@RequestParam(value = "udpsendfq")Integer udpsendfq,
                              @RequestParam(value = "unicast")Boolean unicast,
                              @RequestParam(value = "muticast")Boolean muticast,
                              @RequestParam(value = "ip")String ip,
                              @RequestParam(value = "port")Integer port) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        ifUdpTransferToStop = false;
        if(startedUdpTransfer.containsKey(ip+":"+port)){
            return R.error("UDP转发服务已经开启("+ip+":"+port+")");
        }
        if(muticast){ //组播

        }else { //单播
            UdpServerHandler udphander = new AisUdpSenderHandler(new MessageQ<>(10),UdpMessageSendQ);
            MiiUdpServer udpSender = new MiiUdpSender(udphander);
            dataCollectService.startServer(udpSender);
            startedUdpTransfer.put(ip+":"+port,udpSender);
            Timer timer = new Timer();
            timer.schedule(new TimerTask(){
                @SneakyThrows
                public void run(){
                    if(ifUdpTransferToStop){
                        timer.cancel();
                    }
                    synchronized (UdpMessageSendQ) {
                        for (int i = UdpMessageSendQ.size(); i > 0; i--) {
                            String message = UdpMessageSendQ.remove();
                            sendService.send(ip, port,
                                    message
                                    , udpSender);
                            synchronized (UdpMessageSendedQ) {
                                UdpMessageSendedQ.add("send to(" + ip + ":" + port + "):" + message);
                                UdpMessageSendedQ.setNewDataFlag(true);
                            }
                        }
                        UdpMessageSendedQ.setNewDataFlag(false);
                    }
                }
            },udpsendfq*1000 , udpsendfq*1000);
        }
        return R.ok("转发已开启");
    }


    @RequestMapping(value = "/UdpTransferEventSourceServer",
            produces="text/event-stream;charset=UTF-8")
    public void UdpTransferEventSourceServer(HttpServletRequest request,
                                             HttpServletResponse response) throws InterruptedException, IOException {
        ifUdpTransferToStop = false;
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @SneakyThrows
            public void run(){
                if(ifUdpTransferToStop){
                    timer.cancel();
                }
                response.setContentType("text/event-stream");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(200);
                response.getWriter().write("data:heartbeat\n\n");
                response.getWriter().flush();
            }
        },30000 , 30000);
        String data ="";
        while(!ifUdpTransferToStop){
            TimeUnit.MICROSECONDS.sleep(1);
            if(UdpMessageSendedQ.isNewDataFlag()) {
                response.setContentType("text/event-stream");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(200);
                synchronized (UdpMessageSendedQ) {
                    for (int i = UdpMessageSendedQ.size(); i > 0; i--) {
                        data = UdpMessageSendedQ.remove();
                        response.getWriter().write("data:" + data + "\n\n");
                        response.getWriter().flush();
                    }
                    UdpMessageSendedQ.setNewDataFlag(false);
                  }
                }
            }
            response.getWriter().close();
        }


    /***
     * udp/tcp 接收eventsource
     * @param request
     * @param response
     * @throws IOException
     * @throws InterruptedException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
        private static boolean ifUdpOrTcpReceiveToStop = false;
        @ResponseBody
        @RequestMapping(value="/eventSourceServer",produces="text/event-stream;charset=UTF-8")
        public void eventSourceServer(HttpServletRequest request, HttpServletResponse response) throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        String port = request.getHeader("port");
        Object server = startedServer.get(Integer.parseInt(port));
        Field field =server.getClass().getField("messageQ");
        field.setAccessible(true);
        MessageQ<String> messageQ = (MessageQ<String>)  field.get(server);
        String data = "";
            ifUdpOrTcpReceiveToStop = false;
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @SneakyThrows
            public void run(){
                if(ifUdpOrTcpReceiveToStop){
                    timer.cancel();
                }
                response.setContentType("text/event-stream");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(200);
                response.getWriter().write("data:heartbeat\n\n");
                response.getWriter().flush();
            }
        },30000 , 30000);
        while(!ifUdpOrTcpReceiveToStop){
            TimeUnit.MICROSECONDS.sleep(1);
            if(messageQ.isNewDataFlag()) {
                response.setContentType("text/event-stream");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(200);
                synchronized (messageQ) {
                    for(int i=messageQ.size();i>0;i--) {
                        data = messageQ.remove();
                        response.getWriter().write("data:" + data + "\n\n");
                        response.getWriter().flush();
                    }
                    messageQ.setNewDataFlag(false);
                }
            }
        }
        response.getWriter().close();
    }


        /***
         *  开启UDP  监听端口
         */
        @RequestMapping("/openUDPport")
        public R openUDPport(@RequestParam(value = "port") Integer port) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
            UdpServerHandler handler  = new GpsUdpServerHandler(new MessageQ<>(10),UdpMessageSendQ,HttpMessageSendQ);
        MiiUdpServer udpServer = new MiiUdpServer("0.0.0.0",port,handler);
//        udpServer.start();
        if(startedServer.containsKey(port)){
            return R.error("无法打开状态为以开启端口");
        }
        dataCollectService.startServer(udpServer);
        startedServer.put(port,udpServer);
        Map<String,Object> returnmap = Maps.newHashMap();
        returnmap.put("msg","开启tcp接收端口："+port);
        returnmap.put("port",port);
        return R.ok(returnmap);
    }

        /***
         * 建立TCP 连接
         * @return
         */
        @RequestMapping("/creatTCPServer")
        public R creatTCPConnection(@RequestParam(value = "ipAddress")String ipAddress,
            @RequestParam(value = "port") Integer port) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        MiiServer tcpServer = null;
        if(startedServer.containsKey(port)){
            return R.error("无法打开状态为以开启端口");
        }
        TcpServerHandler handler = new GpsTcpServerHandler(new MessageQ(10),UdpMessageSendQ,HttpMessageSendQ);
        if(null !=ipAddress&&!"".equals(ipAddress)){
            tcpServer =  new MiiServer(ipAddress,port,handler);
        }else {
            tcpServer = new MiiServer(port,handler);
        }
//        tcpServer.addListener(MiiMessage.NMEA, AisDataListener);
        dataCollectService.startServer(tcpServer);
        startedServer.put(port,tcpServer);
        Map<String,Object> returnmap = Maps.newHashMap();
        returnmap.put("msg","开启tcp接收端口："+port);
        returnmap.put("port",port);
        return R.ok(returnmap);
    }


        /***
         * 关闭端口
         */
        /**
         * 关闭
         * @param port
         * @return
         */
        @RequestMapping("/closePort")
        public R stopServer(@RequestParam(value = "port") Integer port) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if( startedServer.containsKey(port)) {
            dataCollectService.stopServer(startedServer.get(port));
            startedServer.remove(port);
            ifUdpOrTcpReceiveToStop = true;
            return R.ok("关闭端口：" + port);
        }else{
            return R.error("无法关闭未打开端口"+ port);
        }
    }
}
