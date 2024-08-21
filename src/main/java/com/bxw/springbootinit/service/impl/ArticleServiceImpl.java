package com.bxw.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bxw.springbootinit.mapper.ArticleMapper;
import com.bxw.springbootinit.model.dto.query.QueryRequest;
import com.bxw.springbootinit.model.entity.Article;
import com.bxw.springbootinit.model.vo.ArticleVO;
import com.bxw.springbootinit.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: ArticleServiceImpl
 * Description:
 * 文章业务实现类
 * @Author 坤坤学🐸
 * @Create 2024/8/21 9:33
 * @Version 1.0
 */
@Service
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

	/**
	 * 文章分页列表
	 * @param title
	 * @param current
	 * @param pageSize
	 * @return
	 */
	@Override
	public Page<ArticleVO> searchArticleList(List<String> title, long current, long pageSize) {
		Page<Article> page = new Page<>(current,pageSize);
		QueryWrapper<Article> queryWrapper = new QueryWrapper<>();
		queryWrapper.in("title",title);
		queryWrapper.orderByDesc("updateTime");
		page = this.page(page,queryWrapper);
		Page<ArticleVO> newPage = new Page<>();
		BeanUtils.copyProperties(page,newPage,"records");
		List<ArticleVO> articleVOList = page.getRecords().stream().map(article -> {
			ArticleVO articleVO = new ArticleVO();
			BeanUtils.copyProperties(article,articleVO);
			return articleVO;
		}).collect(Collectors.toList());
		newPage.setRecords(articleVOList);
		return newPage;
	}


	@Override
	public boolean insertBatchArticles(List<Article> articles) {
		return this.baseMapper.saveArticle(articles);
	}
}
