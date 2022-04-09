package com.takeoff.iot.modbus.netty.message;

import java.io.UnsupportedEncodingException;
import com.takeoff.iot.modbus.common.bytes.factory.MiiDataFactory;
import com.takeoff.iot.modbus.common.data.MiiData;
import com.takeoff.iot.modbus.common.message.MiiMessage;
import com.takeoff.iot.modbus.common.utils.IntegerToByteUtil;

import org.apache.commons.lang3.ArrayUtils;

public class NmeaInMessage implements MiiMessage {

    private String deviceGroup;
    private byte[] msg;
    private MiiData data;

    public NmeaInMessage(byte[] msg, MiiDataFactory dataFactory) throws UnsupportedEncodingException {
        this(null, msg, dataFactory);
    }

    public NmeaInMessage(String deviceGroup, byte[] msg, MiiDataFactory dataFactory) throws UnsupportedEncodingException {
        this.deviceGroup = deviceGroup;
        this.msg = msg;
        this.data = dataFactory.buildData(NmeaInMessage.NMEA,msg);
    }

    public String deviceGroup() {
        return deviceGroup;
    }

    public int command() {
        return MiiMessage.NMEA;
    }

    public int length() {
        byte[] dataLength = ArrayUtils.subarray(msg, DATA_INDEX, COMMAND_INDEX);
        return IntegerToByteUtil.bytesToInt(dataLength);
    }

    public MiiData data() {
        return data;
    }

    public int type() {
        return RECV;
    }

    public byte[] toBytes() {
        return msg;
    }
}
