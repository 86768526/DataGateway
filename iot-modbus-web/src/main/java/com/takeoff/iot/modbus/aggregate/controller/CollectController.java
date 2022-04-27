package com.takeoff.iot.modbus.aggregate.controller;

import com.takeoff.iot.modbus.aggregate.config.IotModbusServerConfig;
import com.takeoff.iot.modbus.aggregate.utils.R;
import com.takeoff.iot.modbus.aggregate.utils.Res;
import com.takeoff.iot.modbus.common.data.MiiData;
import com.takeoff.iot.modbus.common.entity.AlarmLampData;
import com.takeoff.iot.modbus.common.entity.LcdData;
import com.takeoff.iot.modbus.serialport.service.SerialportSendService;
import com.takeoff.iot.modbus.aggregate.properties.IotModbusSerialportProperties;
import com.takeoff.iot.modbus.aggregate.service.DataCollectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/")
public class CollectController {

    @Resource
    private IotModbusServerConfig iotModbusServerConfig;

    @Resource
    private IotModbusSerialportProperties iotModbusSerialportProperties;

    @Resource
    private SerialportSendService serialportSendService;

    @Resource
    private DataCollectService  dataCollectService;

    /**
     * 入口
     * @return
     */
    @RequestMapping("/")
    public Res entrance(){
        Res result= new Res();
        result.setViewName("login.html");
        return result;
    }

    /**
     * 登录接口
     * @return
     */
    @RequestMapping("/tologin")
    public Res index(@RequestParam("account") String account,
                     @RequestParam("password") String password){
        Res result= new Res();
        result.setViewName("index.html");
        return result;
    }

    @RequestMapping("/redar")
    public Res redar(){
        Res result= new Res();
        result.setViewName("rader_index.html");
        return result;
    }

    @RequestMapping("/ais")
    public Res ais(){
        Res result= new Res();
        result.setViewName("ais_index.html");
        return result;
    }

    @RequestMapping("/adsb")
    public Res adsb(){
        Res result= new Res();
        result.setViewName("Adsb_index.html");
        return result;
    }

    @RequestMapping("/index_v1")
    public Res index_v1(){
        Res result= new Res();
        result.setViewName("index_v1.html");
        return result;
    }

    @RequestMapping("/gps")
    public Res gps(){
        Res result= new Res();
        result.setViewName("Gps_index.html");
        return result;
    }

    @RequestMapping("/datasend")
    public Res datasend(){
        Res result= new Res();
        result.setViewName("Dataforward_index.html");
        return result;
    }

    @RequestMapping("/portmonitor")
    public Res portmonitor(){
        Res result= new Res();
        result.setViewName("Portmonitor_index.html");
        return result;
    }


    /**
     * 发送控制单锁指令
     * @param deviceGroup
     * @param device
     * @return
     */
    @RequestMapping("/openlock/{deviceGroup}/{device}")
    public R openLock(@PathVariable("deviceGroup") String deviceGroup, @PathVariable("device") Integer device) {
        if(iotModbusSerialportProperties.getOpen()){
            serialportSendService.unlock(deviceGroup, device);
        }else{
            iotModbusServerConfig.getMiiServer().sender().unlock(deviceGroup, device);
        }
        return R.ok();
    }

    /**
     * 发送控制多开锁指令
     * @param map
     * @return
     */
    @RequestMapping("/openmultilock")
    public R openMultiLock(@RequestBody Map map) {
        if(iotModbusSerialportProperties.getOpen()){
            serialportSendService.unlock(
                    map.get("deviceGroup").toString(), Integer.valueOf(map.get("device").toString()),
                    Integer.valueOf(map.get("lockNo1").toString()), Integer.valueOf(map.get("lockStatus1").toString()),
                    Integer.valueOf(map.get("lockNo2").toString()), Integer.valueOf(map.get("lockStatus2").toString()));
        }else{
            iotModbusServerConfig.getMiiServer().sender().unlock(
                    map.get("deviceGroup").toString(), Integer.valueOf(map.get("device").toString()),
                    Integer.valueOf(map.get("lockNo1").toString()), Integer.valueOf(map.get("lockStatus1").toString()),
                    Integer.valueOf(map.get("lockNo2").toString()), Integer.valueOf(map.get("lockStatus2").toString()));
        }
        return R.ok();
    }

    /**
     * 发送设置扫码模式指令
     * @param deviceGroup
     * @param device
     * @return
     */
    @RequestMapping("/barcode/{deviceGroup}/{device}")
    public R barcode(@PathVariable("deviceGroup") String deviceGroup, @PathVariable("device") Integer device) {
        if(iotModbusSerialportProperties.getOpen()){
            serialportSendService.barcode(deviceGroup, device, MiiData.ONCE);
        }else{
            iotModbusServerConfig.getMiiServer().sender().backlight(deviceGroup, device, MiiData.ONCE);
        }
        return R.ok();
    }

    /**
     * 发送背光灯指令
     * @param deviceGroup
     * @param device
     * @return
     */
    @RequestMapping("/backlight/{deviceGroup}/{device}")
    public R backLight(@PathVariable("deviceGroup") String deviceGroup, @PathVariable("device") Integer device) {
        if(iotModbusSerialportProperties.getOpen()){
            serialportSendService.backlight(deviceGroup, device, MiiData.ON);
        }else{
            iotModbusServerConfig.getMiiServer().sender().backlight(deviceGroup, device, MiiData.ON);
        }
        return R.ok();
    }

    /**
     * 指静脉注册
     * @param cabinetGroup
     * @param cabinet
     * @param fingerId
     * @return
     */
    @RequestMapping("/registerfinger/{cabinetGroup}/{cabinet}/{fingerId}")
    public R registerfinger(@PathVariable("cabinetGroup") String cabinetGroup, @PathVariable("cabinet") Integer cabinet, @PathVariable("fingerId") Integer fingerId) {
        if(iotModbusSerialportProperties.getOpen()){
            serialportSendService.registerFinger(cabinetGroup, cabinet, fingerId);
        }else{
            iotModbusServerConfig.getMiiServer().sender().registerFinger(cabinetGroup, cabinet, fingerId);
        }
        return R.ok();
    }

    /**
     * 批量发送lCD控制指令
     * @param lcdDataList
     * @return
     */
    @RequestMapping("/lcdbatch")
    public R lcdBatch(@RequestBody List<LcdData> lcdDataList) {
        if(iotModbusSerialportProperties.getOpen()){
            serialportSendService.lcdBatch(lcdDataList);
        }else{
            iotModbusServerConfig.getMiiServer().sender().lcdBatch(lcdDataList);
        }
        return R.ok();
    }

    /**
     * 发送三色报警灯指令
     * @param alarmLampData
     * @return
     */
    @RequestMapping("/alarmlamp")
    public void alarmLamp(@RequestBody AlarmLampData alarmLampData){
        if(iotModbusSerialportProperties.getOpen()){
            serialportSendService.alarmLamp(alarmLampData);
        }else{
            iotModbusServerConfig.getMiiServer().sender().alarmLamp(alarmLampData);
        }
    }

}
