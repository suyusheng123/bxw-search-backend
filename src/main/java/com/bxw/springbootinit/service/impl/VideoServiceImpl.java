package com.bxw.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bxw.springbootinit.common.ErrorCode;
import com.bxw.springbootinit.exception.BusinessException;
import com.bxw.springbootinit.mapper.VideoMapper;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.model.entity.Video;
import com.bxw.springbootinit.model.enums.SearchTypeEnum;
import com.bxw.springbootinit.model.vo.ArticleVO;
import com.bxw.springbootinit.model.vo.PictureVO;
import com.bxw.springbootinit.model.vo.VideoVO;
import com.bxw.springbootinit.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 视频服务类实现
 *
 */
@Slf4j
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper,Video> implements VideoService {


    /**
     * 查询视频列表
     * @param id
     * @return
     */
    @Override
    public List<VideoVO> searchVideoList(List<Long> id) {
        return this.baseMapper.searchListFromEs(id);
    }

    @Override
    public boolean insertBatchVideos(List<Video> videos) {
        return this.baseMapper.saveVideo(videos);
    }

	/**
	 * 根据标题查询
	 * @param title
	 * @return
	 */
	@Override
	public List<VideoVO> searchListByTitle(String title) {
		return this.baseMapper.searchList(title);
	}
}
