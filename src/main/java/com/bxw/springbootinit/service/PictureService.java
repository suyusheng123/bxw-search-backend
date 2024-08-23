package com.bxw.springbootinit.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bxw.springbootinit.model.entity.Article;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.model.vo.ArticleVO;
import com.bxw.springbootinit.model.vo.PictureVO;

import java.util.List;

/**
 * 图片查询接口
 *
 */
public interface PictureService extends IService<Picture> {

    /**
     * 图片分页列表
     *
     * @param id
     * @return
     */
    List<PictureVO> searchPictureList(List<Long> id);

    /**
     * 批量插入图片数据
     */

    boolean insertBatchPictures(List<Picture> pictures);

    /**
     * 根据标题查询
     */
    List<PictureVO> searchListByTitle(String title);
}
