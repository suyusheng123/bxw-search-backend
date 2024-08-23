package com.bxw.springbootinit.adapter.datasource.impl;


import com.bxw.springbootinit.adapter.datasource.DataSource;
import com.bxw.springbootinit.model.vo.VideoVO;
import com.bxw.springbootinit.service.AggregatedSearchService;
import com.bxw.springbootinit.service.VideoService;
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
public class VideoDataSourceAdapter implements DataSource {

	@Resource
	private VideoService videoService;

	@Resource
	private AggregatedSearchService aggregatedSearchService;


	@Override
	public List<VideoVO> doSearch(String searchText, long current, long currentSize) {
		return aggregatedSearchService.fetchVideos(searchText,current).stream()
				.map(video -> {
					VideoVO videoVO = new VideoVO();
					videoVO.setId(video.getId());
					videoVO.setTitle(video.getTitle());
					videoVO.setUrl(video.getUrl());
					videoVO.setCover(video.getCover());
                    return videoVO;
				}).collect(Collectors.toList());
	}

	/**
	 * 根据标题查询
	 * @param context
	 * @return
	 */
	@Override
	public List<?> searchListByTitle(String context) {
		return videoService.searchListByTitle(context);
	}
}
