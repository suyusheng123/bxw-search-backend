package com.bxw.springbootinit.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.model.entity.Picture;

/**
 * 图片查询接口
 *
 */
public interface PictureService{
    Page<Picture> searchPicture(String searchText, long page, long pageSize);
}
