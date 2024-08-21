package com.bxw.springbootinit.model.vo;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: ArticleVO
 * Description:
 * 图片返回实体类
 * @Author 坤坤学🐸
 * @Create 2024/8/21 9:41
 * @Version 1.0
 */

@Data
public class PictureVO implements Serializable {

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


}
