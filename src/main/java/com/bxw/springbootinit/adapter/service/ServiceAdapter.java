package com.bxw.springbootinit.adapter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.model.vo.SearchVO;

import java.util.List;
import java.util.Map;

/**
 * ClassName: ServiceAdapter
 * Description:
 * 服务类适配器
 * @Author 坤坤学🐸
 * @Create 2024/8/22 11:07
 * @Version 1.0
 */
public interface ServiceAdapter {

	/**
	 * 根据Es的id查询
	 * @param id
	 * @return
	 */
	List<?> searchDataList(List<Long> id);

	/**
	 * 批量插入数据
	 */
	boolean insertBatchDataList(List<?> dataList);

	/**
	 * 根据title查询
	 */
	SearchVO searchListByTitle(String title,Long current,Long size);


}
