package com.takeoff.iot.modbus.aggregate.controller;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.google.common.collect.Maps;
import com.takeoff.iot.modbus.aggregate.service.DataSendService;
import com.takeoff.iot.modbus.aggregate.utils.R;
import com.takeoff.iot.modbus.netty.handle.MessageQ;
import com.takeoff.iot.modbus.netty.handle.TcpServerHandler;
import com.takeoff.iot.modbus.netty.handle.UdpServerHandler;
import com.takeoff.iot.modbus.netty.handle.ais.AisTcpServerHandler;
import com.takeoff.iot.modbus.netty.handle.ais.AisUdpSenderHandler;
import com.takeoff.iot.modbus.netty.handle.ais.AisUdpServerHandler;
import com.takeoff.iot.modbus.server.MiiServer;
import com.takeoff.iot.modbus.server.MiiUdpSender;
import com.takeoff.iot.modbus.server.MiiUdpServer;
import com.takeoff.iot.modbus.aggregate.service.DataCollectService;
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
import java.sql.Time;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/ais")
public class AisController {

    @Resource
    private DataCollectService dataCollectService;
    @Resource
    private DataSendService sendService;
//    @Resource
//    private AisDataListener AisDataListener;
    public static Map<Integer, Object> startedServer = Maps.newHashMap();
    public static Map<String, Object> startedUdpTransfer = Maps.newHashMap();
    public static MessageQ<String> UdpMessageSendQ = new MessageQ<>(10);
    public static MessageQ<String> UdpMessageSendedQ = new MessageQ<>(10);
    public static MessageQ<String> HttpMessageSendQ = new MessageQ<>(10);
    public static MessageQ<String> HttpMessageSendedQ = new MessageQ<>(10);

    private static boolean ifHttpTransferToStop = false;
    @RequestMapping("/startHttpTransfer")
    @ResponseBody
    public R startHttpTransfer(@RequestParam(value = "httpSendFq") Integer fq,
                               @RequestParam(value = "httpurl")String url){

        ifHttpTransferToStop = false;
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @SneakyThrows
            public void run(){
                if(ifHttpTransferToStop){
                    timer.cancel();
                }
                synchronized (HttpMessageSendQ) {
                    for (int i = HttpMessageSendQ.size(); i > 0; i--) {
                        String message = HttpMessageSendQ.remove();
                        sendService.httpSend(url,
                                message + ",protocal=AI"
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
        ifHttpTransferToStop =false;
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @SneakyThrows
            public void run(){
                if(ifHttpTransferToStop){
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
        while(!ifHttpTransferToStop){
            TimeUnit.MICROSECONDS.sleep(10);
            if(HttpMessageSendedQ.isNewDataFlag()) {
                response.setContentType("text/event-stream");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(200);
                synchronized (HttpMessageSendedQ) {
                for(int i=HttpMessageSendedQ.size();i>0;i--) {
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
                        UdpMessageSendQ.setNewDataFlag(false);
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
                for(int i=UdpMessageSendedQ.size();i>0;i--) {
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

    private static boolean ifUdpOrTcpReceivetostop ;
    /***
     * udp/tcp 接收数据 eventsource
     * @param request
     * @param response
     * @throws IOException
     * @throws InterruptedException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    @ResponseBody
    @RequestMapping(value="/eventSourceServer",produces="text/event-stream;charset=UTF-8")
    public synchronized void eventSourceServer(HttpServletRequest request,HttpServletResponse response) throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        String port = request.getHeader("port");
        Object serverns =  startedServer.get(Integer.parseInt(port));
        Field field =serverns.getClass().getField("messageQ");
        field.setAccessible(true);
        MessageQ<String> messageQ = (MessageQ<String>) field.get(serverns);
        String data = "";
        ifUdpOrTcpReceivetostop = false;
        Timer timer = new Timer();
        Iterator<String> it;
        timer.schedule(new TimerTask(){
            @SneakyThrows
            public void run(){
                if(ifUdpOrTcpReceivetostop){
                    timer.cancel();
                }
                    response.setContentType("text/event-stream");
                    response.setCharacterEncoding("UTF-8");
                    response.setStatus(200);
                    response.getWriter().write("data:heartbeat\n\n");
                    response.getWriter().flush();
            }
        },30000 , 30000);
        while(!ifUdpOrTcpReceivetostop){
            TimeUnit.MICROSECONDS.sleep(1);
            if(messageQ.isNewDataFlag()) {
                response.setContentType("text/event-stream");
                response.setCharacterEncoding("UTF-8");
                response.setStatus(200);
                synchronized (messageQ) {
                for(int i=0;i<messageQ.size();i++) {
                     data = messageQ.get(i);
                     String[] readinfo = data.split("#");
                     if(readinfo.length ==1 ||(!readinfo[1].equals(port))) {
                         response.getWriter().write("data:" + data + "\n\n");
                         response.getWriter().flush();
                         messageQ.set(i, data + "#" + port);
                      }
                    }
                    messageQ.setNewDataFlag(false);
                }
            }
        }
        response.getWriter().close();
    }


    /***
     *
     */
    @RequestMapping("/creatTcpClient")
    public R openTcpClient(@RequestParam(value = "ipAddress")String ipAddress,
                           @RequestParam(value = "port")Integer port){
        if(startedServer.containsKey(port)){
            return R.error("无法打开状态为以开启端口");
        }
        return R.ok("");
    }

    /***
     *  开启UDP  监听端口
     */
    @RequestMapping("/openUDPport")
    public R openUDPport(@RequestParam(value = "port") Integer port) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if(startedServer.containsKey(port)){
            return R.error("无法打开状态为以开启端口");
        }
        UdpServerHandler udphander = new AisUdpServerHandler(new MessageQ(10),UdpMessageSendQ,HttpMessageSendQ);
        MiiUdpServer udpServer = new MiiUdpServer("0.0.0.0",port,udphander);
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
        TcpServerHandler handler = new AisTcpServerHandler(new MessageQ<>(10),UdpMessageSendQ,HttpMessageSendQ);
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
     * 关闭接收端口
     * @param port
     * @return
     */
    @RequestMapping("/closePort")
    public R stopServer(@RequestParam(value = "port") Integer port) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
       if( startedServer.containsKey(port)) {
            dataCollectService.stopServer(startedServer.get(port));
            startedServer.remove(port);
           ifUdpOrTcpReceivetostop = true;
            return R.ok("关闭端口：" + port);
        }else{
           return R.error("无法关闭未打开端口"+ port);
       }
    }

    @RequestMapping(value = "stopUdpTransfer")
    public R stopUdpTransfer(@RequestParam(value = "ip") String ip,
                             @RequestParam(value = "port") Integer port) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if(startedUdpTransfer.containsKey(ip+":"+port)){
            dataCollectService.stopServer(startedUdpTransfer.get(ip+":"+port));
            ifUdpTransferToStop = true;
            startedUdpTransfer.remove(ip+":"+port);
            return R.ok("关闭转发："+ip+":"+port);
        }else{
            return R.error("无法关闭未开启的转发："+ip+":"+port);
        }
    }

    @RequestMapping(value = "stopHttpTransfer")
    public R stopHttpTransfer(){
        ifHttpTransferToStop = false;
        return R.ok("http transfer stop");
    }
}
