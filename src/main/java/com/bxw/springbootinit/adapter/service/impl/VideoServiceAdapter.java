package com.bxw.springbootinit.adapter.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.adapter.service.ServiceAdapter;
import com.bxw.springbootinit.model.entity.Article;
import com.bxw.springbootinit.model.entity.Video;
import com.bxw.springbootinit.model.vo.SearchVO;
import com.bxw.springbootinit.model.vo.VideoVO;
import com.bxw.springbootinit.service.AggregatedSearchService;
import com.bxw.springbootinit.service.PictureService;
import com.bxw.springbootinit.service.VideoService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * ClassName: ArticleServiceAdapter
 * Description:
 * 视频服务适配器
 * @Author 坤坤学🐸
 * @Create 2024/8/22 11:11
 * @Version 1.0
 */
@Component
public class VideoServiceAdapter implements ServiceAdapter {
	@Resource
	private VideoService videoService;


	@Override
	public List<VideoVO> searchDataList(List<Long> id) {
		return videoService.searchVideoList(id);
	}

	@Override
	public boolean insertBatchDataList(List<?> dataList) {
		if(videoService.insertBatchVideos((List<Video>)dataList)) return true;
		return false;
	}

	@Override
	public SearchVO searchListByTitle(String title, Long current, Long size) {
		long offset = (current - 1) * size;
		return videoService.searchListByTitle(title, offset);
	}

}
