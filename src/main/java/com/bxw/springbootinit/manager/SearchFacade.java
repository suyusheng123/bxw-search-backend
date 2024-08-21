package com.bxw.springbootinit.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.common.ErrorCode;
import com.bxw.springbootinit.constant.RedisConstant;
import com.bxw.springbootinit.adapter.datasource.DataSource;
import com.bxw.springbootinit.exception.BusinessException;
import com.bxw.springbootinit.exception.ThrowUtils;
import com.bxw.springbootinit.model.dto.query.QueryRequest;
import com.bxw.springbootinit.model.dto.query.SearchQueryEsRequest;
import com.bxw.springbootinit.model.enums.SearchEnum;
import com.bxw.springbootinit.model.enums.SearchTypeEnum;
import com.bxw.springbootinit.model.vo.AggregatedSearchVO;
import com.bxw.springbootinit.model.vo.SearchVO;
import com.bxw.springbootinit.registry.datasource.DataSourceRegistry;
import com.bxw.springbootinit.service.AggregatedSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ClassName:SearchFacade
 * Package:com.bxw.springbootinit.manager
 * Description:
 * 搜索门面模式
 *
 * @Author 卜翔威
 * @Create 2024/8/14 8:43
 * @Version 1.0
 */

@Component
@Slf4j
public class SearchFacade {

	@Resource
	private DataSourceRegistry dataSourceRegistry;

	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Resource
	private AggregatedSearchService aggregatedSearchService;

	// 线程池
	private final ThreadPoolExecutor esSearchExecutor = new ThreadPoolExecutor(4,
			10,
			10,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(100));

	/**
	 * 聚合搜索接口简易版(直接从数据库拿的数据)
	 * @param queryRequest
	 * @param request
	 * @return
	 */
//	public SearchVO searchAll(@RequestBody QueryRequest queryRequest, HttpServletRequest request) {
//		// 门面模式
//		String searchText = queryRequest.getSearchText();
//		long current = queryRequest.getCurrent();
//		long pageSize = queryRequest.getPageSize();
//		if (searchText == null) {
//			searchText = "";
//		}
//		// 如果前端传入的type为null或者是空字符串,那么就给他一个默认值
//		String type = queryRequest.getType();
//		if (type == null || type.isEmpty()) {
//			type = SearchEnum.PICTURE.getValue();
//		}
//		SearchEnum typeEnum = SearchEnum.getEnumByValue(type);
//		ThrowUtils.throwIf(Objects.isNull(typeEnum), ErrorCode.PARAMS_ERROR);
//		SearchVO searchVO = new SearchVO();
//		// 根据不同的枚举类型来进行相应的查询,注册器模式
//		DataSource<?> dataSource = dataSourceRegistry.getDataSourceByType(type);
//		Page<?> page = dataSource.doSearch(searchText, current, pageSize);
//		searchVO.setDataList(page.getRecords());
//		searchVO.setTotal((int) page.getTotal());
//		return searchVO;
//	}

	/**
	 * 聚合搜索升级版(redis获取数据 + 线程池爬取数据到es然后入库 + redis与mysql的同步数据)
	 *
	 * @param queryRequest
	 * @return
	 */
	public SearchVO searchAll(QueryRequest queryRequest) {
		String type = queryRequest.getType();
		if (StringUtils.isBlank(type)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		SearchTypeEnum enumByValue = SearchTypeEnum.getEnumByValue(type);
		if (enumByValue == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String searchText = queryRequest.getSearchText();
		long current = queryRequest.getCurrent();
		long size = queryRequest.getPageSize();

		SearchVO searchVO = new SearchVO();
		//从redis中获取搜索数据
		String key = RedisConstant.SEARCH_KEY + enumByValue.getValue() + ":" + searchText;
		ListOperations<String, String> list = stringRedisTemplate.opsForList();
		if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
			List<String> cacheSearchList = list.range(key, 0, -1);
			if (CollectionUtils.isNotEmpty(cacheSearchList)) {
				List<AggregatedSearchVO> searchList = cacheSearchList.stream()
						.map(data -> JSONUtil.toBean(data, AggregatedSearchVO.class))
						.collect(Collectors.toList());
				searchVO.setDataList(searchList);
				return searchVO;
			}
		}
		//爬取数据并入库
		CompletableFuture<List<AggregatedSearchVO>> esSearchTask = CompletableFuture.runAsync(() -> {
			DataSource dataSource = dataSourceRegistry.getDataSourceByType(type);
			dataSource.doSearch(searchText,current,size);
		}, esSearchExecutor).thenComposeAsync(data ->
				//es搜索
				CompletableFuture.supplyAsync(() -> {
					SearchQueryEsRequest searchQueryEsRequest = BeanUtil.copyProperties(queryRequest, SearchQueryEsRequest.class, "type");
					searchQueryEsRequest.setType(enumByValue.getType());
					return aggregatedSearchService.aggregatedSearchEs(searchQueryEsRequest);
				}, esSearchExecutor)
		).exceptionally(ex -> {
			log.error("esSearch error = {}", ex.getMessage());
			return new ArrayList<>();
		});
		List<AggregatedSearchVO> searchEsList = esSearchTask.join();
		searchVO.setDataList(searchEsList);
		//将搜索数据缓存到redis中
		if (CollectionUtils.isNotEmpty(searchEsList)) {
			List<String> dataList = searchEsList.stream().map(JSONUtil::toJsonStr).collect(Collectors.toList());
			list.rightPushAll(key, dataList);
			stringRedisTemplate.expire(key, RedisConstant.SEARCH_TTL, TimeUnit.MINUTES);
		}
		return searchVO;
	}
}
