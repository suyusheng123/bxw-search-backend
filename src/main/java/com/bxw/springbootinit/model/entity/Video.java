package com.bxw.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 视频实体
 */

@TableName(value = "video")
@Data
public class Video implements Serializable {

	private static final long serialVersionUID = 1L;


	/**
	 * 视频id
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 视频封面
	 */
	private String cover;

	/**
	 * 视频地址
	 */
	private String url;

	/**
	 * 视频标题
	 */
	private String title;

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


}
