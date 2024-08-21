package com.bxw.springbootinit.adapter.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.model.vo.SearchVO;

/**
 * ClassName:DataSource
 * Package:com.bxw.springbootinit.datasource
 * Description:
 * 统一的数据源接口
 * @Author 卜翔威
 * @Create 2024/8/14 10:33
 * @Version 1.0
 */
public interface DataSource {

//	Page<T> doSearch(String searchText, long current, long pageSize);

	/**
	 * 爬取数据并入库
	 *
	 * @param searchText 搜索词
	 */
	void doSearch(String searchText,long current,long currentSize);
}