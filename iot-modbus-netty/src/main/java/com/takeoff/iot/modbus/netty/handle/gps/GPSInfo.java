package com.takeoff.iot.modbus.netty.handle.gps;

public class GPSInfo {
    public String Longitude;//经度
    public String Latitude; //纬度
    public String Speed;    //速度 单位 节
    public String GPSStatus;//GPS状态 A=数据有效；V=数据无效
    public String GPSTime;//GPS时间
    public String GPSHeading;//航向

    public String toString(){
        return "{Longitude:"+Longitude+"," +
                "Latitude:"+Latitude+"," +
                "Speed:"+Speed+"," +
                "GPSStatus:"+GPSStatus+"," +
                "GPSTime:"+GPSTime+"," +
                "GPSHeading:"+GPSHeading+"}";
    }
}
