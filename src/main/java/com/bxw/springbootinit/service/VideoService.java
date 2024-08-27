package com.bxw.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bxw.springbootinit.mapper.VideoMapper;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.model.entity.Video;
import com.bxw.springbootinit.model.vo.PictureVO;
import com.bxw.springbootinit.model.vo.SearchVO;
import com.bxw.springbootinit.model.vo.VideoVO;

import java.util.List;

/**
 * 视频服务类
 */
public interface VideoService extends IService<Video> {
	/**
	 * 视频分页列表
	 *
	 * @param id
	 * @return
	 */
	List<VideoVO> searchVideoList(List<Long> id);

	/**
	 * 批量插入图片数据
	 */

	boolean insertBatchVideos(List<Video> videos);

	/**
	 * 根据标题查询
	 */
	SearchVO searchListByTitle(String title,Long offset);
}
