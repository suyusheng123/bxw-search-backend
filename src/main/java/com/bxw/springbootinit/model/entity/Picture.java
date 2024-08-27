package com.bxw.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * ClassName:Picture
 * Package:com.bxw.springbootinit.model.entity
 * Description:
 * 图片类
 * @Author 卜翔威
 * @Create 2024/8/7 16:46
 * @Version 1.0
 */

@TableName(value = "picture")
@Data
public class Picture implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	private String title;

	private String url;

	private String sourceUrl;

	private String createTime;

	private String updateTime;

	private String pictureTitleId;
}
