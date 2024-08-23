package com.bxw.springbootinit.model.vo;

import cn.hutool.core.date.DatePattern;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.gson.Gson;
import com.bxw.springbootinit.model.dto.query.AggregatedSearchEsDTO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@Data
public class AggregatedSearchVO implements Serializable {

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
     * 修改时间
     */
    private String updateTime;

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
