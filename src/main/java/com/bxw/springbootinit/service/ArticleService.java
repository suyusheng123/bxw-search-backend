package com.bxw.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bxw.springbootinit.model.entity.Article;
import com.bxw.springbootinit.model.entity.Video;
import com.bxw.springbootinit.model.vo.ArticleVO;

import java.util.List;

/**
 * ClassName: ArticleService
 * Description:
 * 文章业务类
 * @Author 坤坤学🐸
 * @Create 2024/8/21 9:32
 * @Version 1.0
 */
public interface ArticleService extends IService<Article> {

	/**
	 * 文章分页列表
	 *
	 * @param title
	 * @param current
	 * @param pageSize
	 * @return
	 */
	Page<ArticleVO> searchArticleList(List<String> title, long current, long pageSize);

	/**
	 * 批量插入文章数据
	 */

	boolean insertBatchArticles(List<Article> articles);

}
