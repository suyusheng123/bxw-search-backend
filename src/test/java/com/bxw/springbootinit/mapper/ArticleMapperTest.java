package com.bxw.springbootinit.mapper;

import com.bxw.springbootinit.adapter.service.impl.ArticleServiceAdapter;
import com.bxw.springbootinit.model.vo.ArticleVO;
import com.bxw.springbootinit.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: ArticleMapperTest
 * Description:
 * 文章操作类
 * @Author 坤坤学🐸
 * @Create 2024/8/22 12:10
 * @Version 1.0
 */

@SpringBootTest
@Slf4j
public class ArticleMapperTest {

	@Resource
	private ArticleService articleService;

	@Test
	public void testSearchArticles(){
		List<Long> ids = new ArrayList<Long>(){
			{
				add(1826283873143832578L);
				add(1826283873143832579L);
				add(1826283873143832580L);
			}
		};
		List<ArticleVO> articleVOS = articleService.searchArticleList(ids);
		log.info("数据为:{}",articleVOS);
	}
}
