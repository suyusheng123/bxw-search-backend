package com.bxw.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * aggregated_search
 */
@TableName(value = "aggregated_search")
@Data
public class AggregatedSearch implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = -6551888914655007363L;

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
     * url
     */
    private String url;

    /**
     * 来源
     */
    private String sourceUrl;

    /**
     * 封面
     */
    private String cover;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表 json
     */
    private String tags;

    /**
     * 类型[1:帖子 2:图片 3:视频 4:用户]
     */
    private int type;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    public static final String TITLE = "title";
    public static final String URL = "url";
    public static final String SOURCE_URL = "sourceUrl";
    public static final String COVER = "cover";
    public static final String CONTENT = "content";
    public static final String TAGS = "tags";
    public static final String TYPE = "type";
    public static final String USERID = "userId";
    public static final String IS_DELETE = "isDelete";
}
