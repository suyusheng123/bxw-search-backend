package com.bxw.springbootinit.service;

import co.elastic.clients.elasticsearch.sql.QueryRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bxw.springbootinit.model.dto.query.SearchQueryEsRequest;
import com.bxw.springbootinit.model.entity.AggregatedSearch;
import com.bxw.springbootinit.model.vo.AggregatedSearchVO;
import com.bxw.springbootinit.model.vo.PageResult;
import com.bxw.springbootinit.model.vo.SuggestVO;

import java.util.List;

/**
 * ClassName: AggregatedSearchService
 * Description:
 *
 * @Author 坤坤学🐸
 * @Create 2024/8/19 10:06
 * @Version 1.0
 */
public interface AggregatedSearchService extends IService<AggregatedSearch> {

	/**
	 * es 搜索
	 *
	 * @param search 查询条件
	 */
	List<AggregatedSearchVO> aggregatedSearchEs(SearchQueryEsRequest search);

	/**
	 * es 搜索
	 *
	 * @param request 查询条件
	 */
	PageResult aggregatedSearchEsPageList(SearchQueryEsRequest request);

	/**
	 * 搜索建议
	 *
	 * @param keyword 关键词
	 * @return 搜索建议列表
	 */
	List<SuggestVO> getSearchSuggest(String keyword);

	/**
	 * 批量保存 爬取的帖子数据
	 *
	 * @param current 页码
	 */
	void fetchArticles(String searchText,long current);

	/**
	 * 批量保存爬取的图片数据
	 *
	 * @param searchText
	 */
	void fetchPictures(String searchText,long first);

	/**
	 * 批量保存爬取的视频数据
	 *
	 * @param searchText
	 */
	void fetchVideos(String searchText,long first);

	/**
	 * 保存搜索记录和保存爬取的数据
	 *
	 * @param searchQueryRequest 搜索参数
	 */
	void saveSearchTextAndCrawlerData(QueryRequest searchQueryRequest);
}