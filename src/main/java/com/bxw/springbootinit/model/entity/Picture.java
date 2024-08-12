package com.bxw.springbootinit.model.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName:Picture
 * Package:com.bxw.springbootinit.model.entity
 * Description:
 * 图片类
 * @Author 卜翔威
 * @Create 2024/8/7 16:46
 * @Version 1.0
 */

@Data
public class Picture implements Serializable {

	private static final long serialVersionUID = 1L;

	private String title;

	// 图片原始地址
	private String murl;

	// 链接地址
	private String purl;

	// 压缩图片地址
	private String turl;
}
