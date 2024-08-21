package com.bxw.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * ClassName: ArticleVO
 * Description:
 * 视频返回实体类
 * @Author 坤坤学🐸
 * @Create 2024/8/21 9:41
 * @Version 1.0
 */

@Data
public class VideoVO implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private Long id;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * url
	 */
	private String url;

	/**
	 * 封面
	 */
	private String cover;

	/**
	 * 时长
	 */
	private String time;


}
