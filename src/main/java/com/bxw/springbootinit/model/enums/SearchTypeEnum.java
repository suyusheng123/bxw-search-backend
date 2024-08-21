package com.bxw.springbootinit.model.enums;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: SearchTypeEnum
 * Description:
 * 数据源枚举类
 * @Author 坤坤学🐸
 * @Create 2024/8/20 13:45
 * @Version 1.0
 */
public enum SearchTypeEnum {
	Article("文章", "article", 1),
	PICTURE("图片", "picture", 2),
	USER("用户", "user", 3),
	VIDEO("视频", "video", 4);

	private final String text;

	private final String value;

	private final Integer type;

	SearchTypeEnum(String text, String value, Integer type) {
		this.text = text;
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
	public static SearchTypeEnum getEnumByValue(String value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (SearchTypeEnum anEnum : SearchTypeEnum.values()) {
			if (anEnum.value.equals(value)) {
				return anEnum;
			}
		}
		return null;
	}


	public static SearchTypeEnum getEnumByType(Integer value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (SearchTypeEnum anEnum : SearchTypeEnum.values()) {
			if (anEnum.getType().intValue() == value.intValue()) {
				return anEnum;
			}
		}
		return null;
	}

	public static SearchTypeEnum getEnumByValueAndType(String value, Integer type) {
		if (StringUtils.isBlank(value) || type <= 0) {
			return null;
		}
		for (SearchTypeEnum anEnum : SearchTypeEnum.values()) {
			if (anEnum.getValue().equals(value) && anEnum.getType().intValue() == type.intValue()) {
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

	public Integer getType() {
		return type;
	}
}
