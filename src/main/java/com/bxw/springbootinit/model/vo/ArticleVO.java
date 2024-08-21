package com.bxw.springbootinit.model.vo;

import cn.hutool.core.date.DatePattern;
import com.bxw.springbootinit.model.dto.query.AggregatedSearchEsDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.gson.Gson;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * ClassName: ArticleVO
 * Description:
 * 文章返回实体类
 * @Author 坤坤学🐸
 * @Create 2024/8/21 9:41
 * @Version 1.0
 */

@Data
public class ArticleVO implements Serializable {

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
	 * 内容
	 */
	private String content;


	/**
	 * 发布时间
	 */
	private Date publishTime;

}
