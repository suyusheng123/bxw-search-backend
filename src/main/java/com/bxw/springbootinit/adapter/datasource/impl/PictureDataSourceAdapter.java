package com.bxw.springbootinit.adapter.datasource.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.adapter.datasource.DataSource;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.service.AggregatedSearchService;
import com.bxw.springbootinit.service.PictureService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * ClassName:DataSourceAdapter
 * Package:com.bxw.springbootinit.adapter.datasource
 * Description:
 * 图片数据源适配器
 * @Author 卜翔威
 * @Create 2024/8/14 10:37
 * @Version 1.0
 */

@Component
public class PictureDataSourceAdapter implements DataSource {


	@Resource
	private AggregatedSearchService aggregatedSearchService;


	/**
	 * 爬取数据并入库
	 * @param searchText 搜索词
	 */
	@Override
	public void doSearch(String searchText,long current,long currentSize){
		long first = (current - 1) * currentSize + 10;
		aggregatedSearchService.fetchPictures(searchText,first);
	}
}
