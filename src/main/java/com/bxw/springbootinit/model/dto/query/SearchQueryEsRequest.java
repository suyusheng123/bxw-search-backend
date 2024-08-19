package com.bxw.springbootinit.model.dto.query;

import com.bxw.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * es搜索
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SearchQueryEsRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 7556305937832142596L;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 类型
     */
    private Integer type;
}
