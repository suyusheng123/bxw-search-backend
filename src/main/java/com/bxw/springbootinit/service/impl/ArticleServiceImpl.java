package com.bxw.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bxw.springbootinit.mapper.ArticleMapper;
import com.bxw.springbootinit.model.dto.query.QueryRequest;
import com.bxw.springbootinit.model.entity.Article;
import com.bxw.springbootinit.model.vo.ArticleVO;
import com.bxw.springbootinit.model.vo.SearchVO;
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
	 * @param
	 * @return
	 */
	@Override
	public List<ArticleVO> searchArticleList(List<Long> id) {
		List<ArticleVO> articleVOS = this.baseMapper.searchListFromEs(id);
		return articleVOS.stream().sorted((article1, article2) -> {
			// 降序
			return article2.getPublishTime().compareTo(article1.getPublishTime());
		}).collect(Collectors.toList());
	}


	@Override
	public boolean insertBatchArticles(List<Article> articles) {
		return this.baseMapper.saveArticle(articles);
	}

	/**
	 * 根据标题查询(包括记录总数和结果)
	 * @param title
	 * @return
	 */
	@Override
	public SearchVO searchListByTitle(String title, Long offset) {
		Long total = this.baseMapper.searchListCount(title);
		List<ArticleVO> articleVOS = this.baseMapper.searchList(title,offset);
		List<ArticleVO> sortedArticleVOS = articleVOS.stream().sorted((article1, article2) -> {
			// 降序
			return article2.getPublishTime().compareTo(article1.getPublishTime());
		}).collect(Collectors.toList());
		SearchVO searchVO = new SearchVO();
		searchVO.setTotal(total);
		searchVO.setDataList(sortedArticleVOS);
		return searchVO;
	}
}
