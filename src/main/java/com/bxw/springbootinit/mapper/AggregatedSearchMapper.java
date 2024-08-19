package com.bxw.springbootinit.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bxw.springbootinit.model.entity.AggregatedSearch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * aggregated_search mapper
 *
 * @author aliterc
 */
@Mapper
public interface AggregatedSearchMapper extends BaseMapper<AggregatedSearch> {

    /**
     * 批量保存搜索数据（如果标题(title)重复则会更新内容(content)）
     *
     * @param aggregatedSearches 数据列表
     * @return boolean
     */
    boolean saveAggregatedSearchList(@Param("searchList") List<AggregatedSearch> aggregatedSearches);
}




