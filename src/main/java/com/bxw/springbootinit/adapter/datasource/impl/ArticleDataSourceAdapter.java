package com.bxw.springbootinit.adapter.datasource.impl;


import com.bxw.springbootinit.adapter.datasource.DataSource;
import com.bxw.springbootinit.service.AggregatedSearchService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * ClassName:DataSourceAdapter
 * Package:com.bxw.springbootinit.adapter.datasource
 * Description:
 * 用户数据源适配器
 * @Author 卜翔威
 * @Create 2024/8/14 10:37
 * @Version 1.0
 */

@Component
public class ArticleDataSourceAdapter implements DataSource{


	@Resource
	private AggregatedSearchService aggregatedSearchService;


	/**
	 * 爬取数据并入库
	 * @param searchText 搜索词
	 */
	@Override
	public void doSearch(String searchText,long current,long currentSize){
		aggregatedSearchService.fetchArticles(searchText,current);
	}
}
