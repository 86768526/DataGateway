package com.takeoff.iot.modbus.common.bytes.factory;


public class MiiLcdBatchBytesFactory extends MiiInteger2BytesFactory<Integer> implements MiiBytesFactory<Integer> {

    private static final int LCD_BATCH_BYTES = 1, LCD_BATCH_COUNT = 4;
	
	public MiiLcdBatchBytesFactory() {
		this(0);
	}
	
	public MiiLcdBatchBytesFactory(int startPos) {
		super(LCD_BATCH_BYTES, LCD_BATCH_COUNT, startPos);
	}

}
