package com.bxw.springbootinit.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页实体
 *
 */
@Data
public class PageResult {

    private long total;

    private List<?> data;
}
