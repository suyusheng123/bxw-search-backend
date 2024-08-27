package com.bxw.springbootinit.service;


import com.bxw.springbootinit.model.dto.query.QueryRequest;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bxw.springbootinit.model.dto.query.SearchQueryEsRequest;
import com.bxw.springbootinit.model.entity.AggregatedSearch;
import com.bxw.springbootinit.model.entity.Article;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.model.entity.Video;
import com.bxw.springbootinit.model.vo.*;

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
	SearchVO aggregatedSearchEs(SearchQueryEsRequest search);


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
	SearchVO fetchArticles(String searchText, long current);

	/**
	 * 批量保存爬取的图片数据
	 *
	 * @param searchText
	 */
	SearchVO fetchPictures(String searchText, long first);

	/**
	 * 批量保存爬取的视频数据
	 *
	 * @param searchText
	 */
	SearchVO fetchVideos(String searchText, long first);

	/**
	 * 批量保存爬取的数据
	 *
	 */
	void saveSearchTextAndCrawlerData(List<AggregatedSearch> searches,List<?> dataList,String type);
}
