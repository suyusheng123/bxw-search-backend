package com.bxw.springbootinit.adapter.method.aggregatedservice.impl;

import com.bxw.springbootinit.adapter.method.aggregatedservice.FetchDataMethodAdapter;
import com.bxw.springbootinit.model.vo.SearchVO;
import com.bxw.springbootinit.service.AggregatedSearchService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * ClassName: AggregatedServiceAdapterImpl
 * Description:
 *
 * @Author 坤坤学🐸
 * @Create 2024/8/25 16:47
 * @Version 1.0
 */
@Component
public class FetchPicturesMethodAdapterImpl implements FetchDataMethodAdapter {
	@Resource
	private AggregatedSearchService aggregatedSearchService;
	@Override
	public SearchVO fetchData(String searchText, long first) {
		long current = (first - 1) * 10 + 35;
		return aggregatedSearchService.fetchPictures(searchText, current);
	}
}
