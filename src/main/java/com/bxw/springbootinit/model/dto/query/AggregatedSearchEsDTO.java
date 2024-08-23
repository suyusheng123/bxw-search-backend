package com.bxw.springbootinit.model.dto.query;

import cn.hutool.core.date.DatePattern;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.bxw.springbootinit.model.entity.AggregatedSearch;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 */
@Slf4j
@Document(indexName = "aggregated_search")
@Data
public class AggregatedSearchEsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Id
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 标题搜索提示
     */
    private String titleSuggest;



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
    @Field(index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Field(index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private LocalDateTime updateTime;


    private static final Gson GSON = new Gson();

    /**
     * 对象转包装类
     *
     * @param search 实体
     * @return {@link AggregatedSearchEsDTO}
     */
    public static AggregatedSearchEsDTO objToDto(AggregatedSearch search) {
        if (search == null) {
            return null;
        }
        AggregatedSearchEsDTO esDTO = new AggregatedSearchEsDTO();
        esDTO.setTitleSuggest(search.getTitle());
        BeanUtils.copyProperties(search, esDTO);
        log.info("esDto = {}", esDTO);
        return esDTO;
    }

    /**
     * 包装类转对象
     *
     * @param esDTO 搜索实体
     * @return {@link AggregatedSearch}
     */
    public static AggregatedSearch dtoToObj(AggregatedSearchEsDTO esDTO) {
        if (esDTO == null) {
            return null;
        }
        AggregatedSearch search = new AggregatedSearch();
        BeanUtils.copyProperties(esDTO, search);
        return search;
    }
}
