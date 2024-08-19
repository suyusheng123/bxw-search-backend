package com.bxw.springbootinit.model.vo;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.gson.Gson;
import com.bxw.springbootinit.model.dto.query.AggregatedSearchEsDTO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class AggregatedSearchVO implements Serializable {

    private static final long serialVersionUID = -5603646150822509988L;

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
    private List<String> tags;

    /**
     * 类型[1:帖子 2:图片 3:视频 4:用户]
     */
    private int type;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 用户
     */
    private UserVO user;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN)
    private LocalDateTime updateTime;

    private static final Gson GSON = new Gson();

    public static AggregatedSearchVO objToVo(AggregatedSearchEsDTO esDTO) {
        if (esDTO == null) {
            return null;
        }
        AggregatedSearchVO searchVO = new AggregatedSearchVO();
        BeanUtils.copyProperties(esDTO, searchVO);
        return searchVO;
    }
}
