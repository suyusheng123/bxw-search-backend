package com.bxw.springbootinit.model.enums;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: SearchTypeEnum
 * Description:
 * mq同步Redis和mysql枚举类
 * @Author 坤坤学🐸
 * @Create 2024/8/20 13:45
 * @Version 1.0
 */
public enum OperationTypeEnum {
	REDIS("redis", 1),
	MYSQL("mysql", 2);


	private final String value;

	private final Integer type;

	OperationTypeEnum(String value, Integer type) {
		this.value = value;
		this.type = type;
	}

	/**
	 * 获取值列表
	 *
	 * @return
	 */
	public static List<String> getValues() {
		return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
	}

	/**
	 * 根据 value 获取枚举
	 *
	 * @param value
	 * @return
	 */
	public static OperationTypeEnum getEnumByValue(String value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (OperationTypeEnum anEnum : OperationTypeEnum.values()) {
			if (anEnum.value.equals(value)) {
				return anEnum;
			}
		}
		return null;
	}


	public static OperationTypeEnum getEnumByType(Integer value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (OperationTypeEnum anEnum : OperationTypeEnum.values()) {
			if (anEnum.getType().intValue() == value.intValue()) {
				return anEnum;
			}
		}
		return null;
	}

	public static OperationTypeEnum getEnumByValueAndType(String value, Integer type) {
		if (StringUtils.isBlank(value) || type <= 0) {
			return null;
		}
		for (OperationTypeEnum anEnum : OperationTypeEnum.values()) {
			if (anEnum.getValue().equals(value) && anEnum.getType().intValue() == type.intValue()) {
				return anEnum;
			}
		}
		return null;
	}

	public String getValue() {
		return value;
	}

	public Integer getType() {
		return type;
	}
}
