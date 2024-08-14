package com.bxw.springbootinit.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 聚合返回类
 *
 */
@Data
public class SearchVO implements Serializable {

    private static final long serialVersionUID = 1L;
    // 记录总数
    private long total;

    // 添加一个统一的集合
    private List<?> dataList;
}
