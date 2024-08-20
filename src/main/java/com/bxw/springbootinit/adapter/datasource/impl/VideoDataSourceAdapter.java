package com.bxw.springbootinit.adapter.datasource.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.adapter.datasource.DataSource;
import com.bxw.springbootinit.model.dto.post.PostQueryRequest;
import com.bxw.springbootinit.model.entity.AggregatedSearch;
import com.bxw.springbootinit.model.entity.Post;
import com.bxw.springbootinit.model.entity.Video;
import com.bxw.springbootinit.model.vo.PostVO;
import com.bxw.springbootinit.service.AggregatedSearchService;
import com.bxw.springbootinit.service.PostService;
import com.bxw.springbootinit.service.VideoService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
public class VideoDataSourceAdapter implements DataSource<Video> {

	@Resource
	private VideoService videoService;

	@Resource
	private AggregatedSearchService aggregatedSearchService;

//	@Override
//	public Page< doSearch(String searchText, long current, long size) {
//		Page<Video> videoPage = videoService.searchVideoPageList(searchText, current, size);
//		if (ObjectUtils.isEmpty(videoPage)) {
//			return new Page<>(current, size);
//		}
//		return videoPage;
//	}

	@Override
	public void doSearch(String searchText,long current,long currentSize) {
		long first = (current - 1) * currentSize + 35;
		aggregatedSearchService.fetchVideoPassage(searchText,first);
	}
}
