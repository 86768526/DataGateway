package com.takeoff.iot.modbus.netty.handle.gps;

import java.math.BigDecimal;

public class GPSAnalysis {

    /// <summary>
    /// GNRMC解析[北斗]
    /// </summary>
    /// <param name="_RecString">原始字符串</param>
    /// <returns>北斗定位信息</returns
/*
$GNRMC,
092846.400, // UTC时间，hhmmss.sss(时分秒.毫秒)格式
A, // 定位状态，A=有效定位，V=无效定位
3029.7317,N, // 纬度
10404.1784,E, // 经度
000.0, // 地面速率
183.8, // 地面航向
070417, // UTC日期
, // 磁俯角
, // 磁方向角
A*73 // 模式指示
*/

    public static GPSInfo GNRMCAnalysis(String strData)
    {
        GPSInfo gpsInfo = null;
            String[] strSplits = strData.split(",");
            if (strSplits.length >= 12)
            {
                if (strSplits[0].equals("$GNRMC"))
                {
                    gpsInfo = new GPSInfo();
                    gpsInfo.GPSStatus = strSplits[2];
                    gpsInfo.GPSHeading = strSplits[8];
                    gpsInfo.Speed = strSplits[7].equals("") ? "" :strSplits[7] ; //Convert.ToDouble(Convert.ToDouble(strSplits[7]) * 1.852).ToString("0.0")
                    gpsInfo.Latitude = strSplits[3].equals("") ? "" : GPSTransforming(strSplits[3]);
                    gpsInfo.Longitude = strSplits[5].equals("") ? "" : GPSTransforming(strSplits[5]);
                    gpsInfo.GPSTime = strSplits[9].equals("") ? "" : "20" + strSplits[9].substring(4, 6) + "-" + strSplits[9].substring(2, 4) + "-" + strSplits[9].substring(0, 2) + " " + strSplits[1].substring(0, 2) + ":" + strSplits[1].substring(2, 4) + ":" + strSplits[1].substring(4, 6);
                }
            }
        return gpsInfo;
    }

    /// <summary>
    /// GPRM字符串解析[GPS]
    /// </summary>
    /// <param name="_RecString">原始字符串</param>
    /// <returns>GPS定位信息</returns>
//    public static GPSInfo GPRMCAnalysis(String _RecString)
//    {
//        GPSInfo gpsInfo = null;
//        if (!String.IsNullOrEmpty(_RecString))
//        {
//            _RecString = _RecString.Contains("\r") ? _RecString.SubString(0, _RecString.IndexOf("\r")) : _RecString;
//            String[] seg = _RecString.split(',');
//            if (seg.Length >= 12)
//            {
//                gpsInfo = new GPSInfo();
//                gpsInfo.GPSStatus = seg[2];//状态
//                gpsInfo.GPSHeading = seg[8];//角度
//                gpsInfo.Speed = seg[7] == "" ? "" : (Convert.ToDouble(seg[7]) * 1.852).ToString("0.0");//速度
//                gpsInfo.Latitude = seg[4] == "" ? "" : GPSTransforming(seg[3]).ToString("0.000000");
//                gpsInfo.Longitude = seg[6] == "" ? "" : GPSTransforming(seg[5]).ToString("0.000000"); ;
//                gpsInfo.GPSTime = seg[9] == "" ? "" : String.Format("20{0}-{1}-{2} {3}:{4}:{5}", seg[9].SubString(4), seg[9].SubString(2, 2), seg[9].SubString(0, 2), seg[1].SubString(0, 2), seg[1].SubString(2, 2), seg[1].SubString(4));
//            }
//        }
//        return gpsInfo;
//    }

    /// <summary>
    /// 降度分秒格式经纬度转换为小数经纬度
    /// </summary>
    /// <param name="_Value">度分秒经纬度</param>
    /// <returns>小数经纬度</returns>
    private static String GPSTransforming(String _Value)
    {
        String Ret = "";

        String[] TempStr = _Value.split("\\.");
        String xStr =  TempStr[0].substring(0, TempStr[0].length() - 2);
        BigDecimal x = new BigDecimal(xStr);
        xStr = TempStr[0].substring(TempStr[0].length() - 2, TempStr[0].length());
        BigDecimal y = new BigDecimal(xStr);
        BigDecimal z = new BigDecimal(TempStr[1].substring(0, 4));
        z = z.divide(BigDecimal.valueOf(600000),6,BigDecimal.ROUND_HALF_DOWN);
        y = y.divide(BigDecimal.valueOf(60),6,BigDecimal.ROUND_HALF_DOWN);
        x = x.add(y).add(z);
//        Ret = Double.valueOf(x) + Double.valueOf(y) / 60 + Double.valueOf(z) / 600000;
        Ret = x.setScale(6,BigDecimal.ROUND_HALF_DOWN).toString();
        return Ret;
    }
}
