package com.bxw.springbootinit.model.dto.query;

import com.bxw.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 聚合查询接口
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String searchText;

    private String type;
}
