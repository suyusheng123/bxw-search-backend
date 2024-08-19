package com.bxw.springbootinit.service;

import com.bxw.springbootinit.model.dto.query.AggregatedSearchEsDTO;
import com.bxw.springbootinit.model.dto.query.SearchQueryEsRequest;
import com.bxw.springbootinit.model.entity.AggregatedSearch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import javax.annotation.Resource;

/**
 * ClassName: AggretedSeachTest
 * Description:
 * 测试类
 * @Author 坤坤学🐸
 * @Create 2024/8/19 14:11
 * @Version 1.0
 */

@SpringBootTest
@Slf4j
public class AggretedSeachTest {
	@Resource
	private ElasticsearchRestTemplate elasticsearchRestTemplate;

	@Test
	public void testSearchAll(){
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().build();
		SearchHits<AggregatedSearchEsDTO> search = elasticsearchRestTemplate.search(searchQuery, AggregatedSearchEsDTO.class);
		log.info("search: {}", search.getSearchHits());
	}

}
