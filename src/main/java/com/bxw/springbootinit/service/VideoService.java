package com.bxw.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.model.entity.Video;

/**
 * 视频服务类
 *
 */
public interface VideoService {
    /**
     * 视频分页列表
     *
     * @param searchText
     * @param current
     * @param pageSize
     * @return
     */
    Page<Video> searchVideoPageList(String searchText, long current, long pageSize);
}
