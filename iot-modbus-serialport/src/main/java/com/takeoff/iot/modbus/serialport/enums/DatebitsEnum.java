package com.takeoff.iot.modbus.serialport.enums;


public enum DatebitsEnum {

	FIVE(5, "FIVE"),
	SIX(6, "SIX"),
	SEVEN(7, "SEVEN"),
	EIGHT(8, "EIGHT"),
	;
	
	// 成员变量
	private Integer key;
	private String value;

	// 构造方法
	private DatebitsEnum(Integer key, String value) {
			this.key = key;
			this.value = value;
		}

	// 普通方法
	public static String getName(Integer key) {
		for (DatebitsEnum c : DatebitsEnum.values()) {
			if (c.getKey().equals(key)) {
				return c.value;
			}
		}
		return null;
	}

	public Integer getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}
}
