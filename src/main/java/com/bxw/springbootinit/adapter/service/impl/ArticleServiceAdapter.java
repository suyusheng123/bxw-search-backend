package com.bxw.springbootinit.adapter.service.impl;

import com.bxw.springbootinit.adapter.service.ServiceAdapter;
import com.bxw.springbootinit.model.entity.Article;
import com.bxw.springbootinit.service.ArticleService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.bxw.springbootinit.model.enums.SearchTypeEnum.Article;

/**
 * ClassName: ArticleServiceAdapter
 * Description:
 * 文章服务适配器
 * @Author 坤坤学🐸
 * @Create 2024/8/22 11:11
 * @Version 1.0
 */

@Component
public class ArticleServiceAdapter implements ServiceAdapter {
	@Resource
	private ArticleService articleService;

	@Override
	public List<?> searchDataList(List<Long> id) {
		return articleService.searchArticleList(id);
	}

	@Override
	public boolean insertBatchDataList(List<?> dataList) {
		if(articleService.insertBatchArticles((List<Article>)dataList)) return true;
		return false;
	}
}
