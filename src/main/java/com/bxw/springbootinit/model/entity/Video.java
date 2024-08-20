package com.bxw.springbootinit.model.entity;

import lombok.Data;

import java.io.Serializable;
import java.sql.Time;

/**
 * 视频实体
 *
 */
@Data
public class Video implements Serializable {

    private static final long serialVersionUID = 3728174777964408682L;

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
     * 视频内容
     */
    private String content;

    /**
     * 视频时间
     */
    private String time;
}
