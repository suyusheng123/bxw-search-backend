package com.bxw.springbootinit.model.entity;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * aggregated_search
 */
@TableName(value = "aggregated_search")
@Data
public class AggregatedSearch implements Serializable {

	@TableField(exist = false)
	private static final long serialVersionUID = 1L;

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
	 * 类型[1:帖子 2:图片 3:视频 4:用户]
	 */
	private int type;


	/**
	 * 创建时间
	 */
	private String createTime;

	/**
	 * 更新时间
	 */
	private String updateTime;


	public static final String TITLE = "title";
	public static final String CONTENT = "content";
	public static final String TYPE = "type";

	private String aggregatedTitleId;
}
