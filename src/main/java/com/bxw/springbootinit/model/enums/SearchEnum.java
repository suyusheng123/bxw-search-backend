package com.bxw.springbootinit.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聚合接口传入的参数枚举类
 */
public enum SearchEnum {

	/**
	 * 文章
	 */
	POST("文章", "post"),
	/**
	 * 用户
	 */
	USER("用户", "user"),
	/**
	 * 图片
	 */
	PICTURE("图片", "picture");

	private final String text;

	private final String value;

	SearchEnum(String text, String value) {
		this.text = text;
		this.value = value;
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
	public static SearchEnum getEnumByValue(String value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (SearchEnum anEnum : SearchEnum.values()) {
			if (anEnum.value.equals(value)) {
				return anEnum;
			}
		}
		return null;
	}

	public String getValue() {
		return value;
	}

	public String getText() {
		return text;
	}
}
