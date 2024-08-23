package com.bxw.springbootinit.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bxw.springbootinit.MainApplication;
import com.bxw.springbootinit.common.ErrorCode;
import com.bxw.springbootinit.exception.BusinessException;
import com.bxw.springbootinit.mapper.PictureMapper;
import com.bxw.springbootinit.model.entity.Article;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.model.vo.ArticleVO;
import com.bxw.springbootinit.model.vo.PictureVO;
import com.bxw.springbootinit.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * ClassName:PictureServiceImpl
 * Package:com.bxw.springbootinit.service.impl
 * Description:
 * 图片服务实现类
 * @Author 卜翔威
 * @Create 2024/8/7 17:08
 * @Version 1.0
 */

@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper,Picture>implements PictureService {


	@Override
	public boolean insertBatchPictures(List<Picture> pictures) {
		return this.baseMapper.savePicture(pictures);
	}

	/**
	 * 根据标题查询
	 * @param title
	 * @return
	 */
	@Override
	public List<PictureVO> searchListByTitle(String title) {
		return this.baseMapper.searchList(title);
	}

	@Override
	public List<PictureVO> searchPictureList(List<Long> id) {
		return this.baseMapper.searchListFromEs(id);
	}
}
