package com.bxw.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bxw.springbootinit.model.entity.AggregatedSearch;
import com.bxw.springbootinit.model.entity.Article;
import com.bxw.springbootinit.model.vo.ArticleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * ClassName: ArticleMapper
 * Description:
 * 文章数据库类
 *
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

	/**
	 * 根据es提供的id进行查询
	 */
	@Select({
			"<script>",
			"SELECT id,url,publishTime FROM article WHERE id IN",
			"<foreach item='item' index='index' collection='id' open='(' separator=',' close=')'>",
			"#{item}",
			"</foreach>",
			"</script>"
	})
	List<ArticleVO> searchListFromEs(@Param("id") List<Long> id);

	/**
	 * 根据标题进行查询,如果标题为空,那么查询最新的前10条数据
	 */
	@Select({
			"<script>",
			"SELECT id,url,title,date_format(publishTime,'%Y-%m-%d %H:%i:%s') as publishTime FROM article",
			"<where>",
			"<if test=\"title != null and title != ''\">",
			"AND title LIKE CONCAT('%', #{title}, '%')",
			"</if>",
			"</where>",
			"order by publishTime desc",
			"LIMIT 10",
			"</script>"
	})
	List<ArticleVO> searchList(@Param("title") String title);
}
