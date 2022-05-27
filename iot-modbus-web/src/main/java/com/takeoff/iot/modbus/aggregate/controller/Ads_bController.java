package com.takeoff.iot.modbus.aggregate.controller;

import com.google.common.collect.Maps;
import com.takeoff.iot.modbus.aggregate.service.DataCollectService;
import com.takeoff.iot.modbus.aggregate.service.DataSendService;
import com.takeoff.iot.modbus.aggregate.utils.R;
import com.takeoff.iot.modbus.netty.handle.MessageQ;
import com.takeoff.iot.modbus.netty.handle.UdpServerHandler;
import com.takeoff.iot.modbus.netty.handle.ads_b.AdsbUdpServerHandler;
import com.takeoff.iot.modbus.netty.handle.ais.AisUdpServerHandler;
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
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/adsb")
public class Ads_bController {

    public static Map<Integer, Object> startedServer = Maps.newHashMap();
    @Resource
    private DataCollectService dataCollectService;
    @Resource
    private DataSendService sendService;
    public static MessageQ<String> UdpMessageSendQ = new MessageQ<>(10);
    public static MessageQ<String> UdpMessageSendedQ = new MessageQ<>(10);
    public static MessageQ<String> HttpMessageSendQ = new MessageQ<>(10);
    public static MessageQ<String> HttpMessageSendedQ = new MessageQ<>(10);
    /***
     *  开启UDP  监听端口
     */
    @RequestMapping("/openUDPport")
    public R openUDPport(@RequestParam(value = "port") Integer port) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if(startedServer.containsKey(port)){
            return R.error("无法打开状态为以开启端口");
        }
        UdpServerHandler udphander = new AdsbUdpServerHandler(new MessageQ(10),UdpMessageSendQ,HttpMessageSendQ);
        MiiUdpServer udpServer = new MiiUdpServer("0.0.0.0",port,udphander);
        dataCollectService.startServer(udpServer);
        startedServer.put(port,udpServer);
        Map<String,Object> returnmap = Maps.newHashMap();
        returnmap.put("msg","开启UDP接收端口："+port);
        returnmap.put("port",port);
        return R.ok(returnmap);
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
    public synchronized void eventSourceServer(HttpServletRequest request, HttpServletResponse response) throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
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

    /***
     * http 转发 eventsource
     * @param request
     * @param response
     * @throws InterruptedException
     * @throws IOException
     */
    private static boolean ifHttpTransferToStop = false;
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

    /***
     *  http 定时转发
     * @param fq
     * @param url
     * @return
     */
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
                                message + ",protocal=ADS"
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

    /***
     * 关闭HTTP 转发
     * @return
     */
    @RequestMapping(value = "stopHttpTransfer")
    public R stopHttpTransfer(){
        ifHttpTransferToStop = false;
        return R.ok("http transfer stop");
    }

}
