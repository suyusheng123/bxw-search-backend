package com.bxw.springbootinit.adapter.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.adapter.service.ServiceAdapter;
import com.bxw.springbootinit.model.entity.Article;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.model.vo.PictureVO;
import com.bxw.springbootinit.service.ArticleService;
import com.bxw.springbootinit.service.PictureService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * ClassName: ArticleServiceAdapter
 * Description:
 * 图片服务适配器
 * @Author 坤坤学🐸
 * @Create 2024/8/22 11:11
 * @Version 1.0
 */
@Component
public class PictureServiceAdapter implements ServiceAdapter {
	@Resource
	private PictureService pictureService;

	@Override
	public List<PictureVO> searchDataList(List<Long> id) {
		return pictureService.searchPictureList(id);
	}

	@Override
	public boolean insertBatchDataList(List<?> dataList) {
		if(pictureService.insertBatchPictures((List<Picture>)dataList)) return true;
		return false;
	}
}
