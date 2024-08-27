package com.bxw.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * ClassName: Article
 * Description:
 * 文章实体类
 * @Author 坤坤学🐸
 * @Create 2024/8/21 8:46
 * @Version 1.0
 */

@TableName(value = "article")
@Data
public class Article implements Serializable {
	/**
	 * id
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 内容
	 */
	private String content;

	/**
	 * 来源
	 */
	private String url;

	/**
	 * 爬虫网站
	 */
	private String sourceUrl;



	/**
	 * 创建时间
	 */
	private String createTime;

	/**
	 * 更新时间
	 */
	private String updateTime;

	/**
	 * 发布时间
	 */
	private String publishTime;

	/**
	 * 根据标题生成的唯一id
	 */
	private String articleTitleId;



	@TableField(exist = false)
	private static final long serialVersionUID = 1L;
}
