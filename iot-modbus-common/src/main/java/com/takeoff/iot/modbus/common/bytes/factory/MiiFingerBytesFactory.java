package com.takeoff.iot.modbus.common.bytes.factory;


public class MiiFingerBytesFactory extends MiiInteger2BytesFactory<Integer> implements MiiBytesFactory<Integer> {

	private static final int FINGER_BYTES = 1,FINGER_COUNT = 10;

	public MiiFingerBytesFactory() {
		this(0);
	}

	public MiiFingerBytesFactory(int startPos) {
		super(FINGER_BYTES, FINGER_COUNT, startPos);
	}
}
