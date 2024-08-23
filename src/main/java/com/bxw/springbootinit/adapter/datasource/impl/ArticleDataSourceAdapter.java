package com.bxw.springbootinit.adapter.datasource.impl;


import com.bxw.springbootinit.adapter.datasource.DataSource;
import com.bxw.springbootinit.model.vo.ArticleVO;
import com.bxw.springbootinit.service.AggregatedSearchService;
import com.bxw.springbootinit.service.ArticleService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

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
	private ArticleService articleService;

	@Resource
	private AggregatedSearchService aggregatedSearchService;


	/**
	 * 爬取数据并入库
	 * @param searchText 搜索词
	 */
	@Override
	public List<ArticleVO> doSearch(String searchText, long current, long currentSize){
		return aggregatedSearchService.fetchArticles(searchText,current).stream().map(article -> {
			ArticleVO articleVO = new ArticleVO();
			articleVO.setId(article.getId());
			articleVO.setTitle(article.getTitle());
			articleVO.setUrl(article.getUrl());
			articleVO.setContent(article.getContent());
			articleVO.setPublishTime(article.getPublishTime());
            return articleVO;
		}).collect(Collectors.toList());
	}

	/**
	 * 根据标题查询
	 * @param context
	 * @return
	 */
	@Override
	public List<?> searchListByTitle(String context) {
		return articleService.searchListByTitle(context);
	}
}
