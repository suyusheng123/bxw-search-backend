package com.bxw.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bxw.springbootinit.model.entity.AggregatedSearch;
import com.bxw.springbootinit.model.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ClassName: ArticleMapper
 * Description:
 * 文章数据库类
 * @Author 坤坤学🐸
 * @Create 2024/8/21 9:03
 * @Version 1.0
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
	/**
	 * 批量保存搜索数据（如果标题(title)重复则会更新内容(content)）
	 *
	 * @param articles 数据列表
	 * @return boolean
	 */
	boolean saveArticle(@Param("searchList") List<Article> articles);
}
