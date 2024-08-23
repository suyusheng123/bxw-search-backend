package com.bxw.springbootinit.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

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



}
