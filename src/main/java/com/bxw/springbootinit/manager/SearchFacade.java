package com.bxw.springbootinit.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.bxw.springbootinit.adapter.datasource.DataSource;
import com.bxw.springbootinit.adapter.service.ServiceAdapter;
import com.bxw.springbootinit.common.ErrorCode;
import com.bxw.springbootinit.constant.RedisConstant;
import com.bxw.springbootinit.exception.BusinessException;
import com.bxw.springbootinit.model.dto.mq.InsertBatchDTO;
import com.bxw.springbootinit.model.dto.query.QueryRequest;
import com.bxw.springbootinit.model.dto.query.SearchQueryEsRequest;
import com.bxw.springbootinit.model.enums.OperationTypeEnum;
import com.bxw.springbootinit.model.enums.SearchTypeEnum;
import com.bxw.springbootinit.model.vo.AggregatedSearchVO;
import com.bxw.springbootinit.model.vo.SearchVO;
import com.bxw.springbootinit.mq.SearchMessageProducer;
import com.bxw.springbootinit.registry.datasource.DataSourceRegistry;
import com.bxw.springbootinit.registry.service.TypeServiceRegistry;
import com.bxw.springbootinit.service.AggregatedSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.internal.StringUtil;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
	private SearchMessageProducer searchMessageProducer;


	@Resource
	private TypeServiceRegistry typeServiceRegistry;

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
	 * 聚合搜索升级版(redis抗压 + 线程池爬取数据到mysql+ mysql同步es + es高亮 + 数据组合)
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
		log.info("开始从Redis中查询数据...");
		//从redis中获取搜索数据
		String key = RedisConstant.SEARCH_KEY + enumByValue.getValue() + ":" + searchText;
		ListOperations<String, String> list = stringRedisTemplate.opsForList();
		if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
			List<String> cacheSearchList = list.range(key, 0, -1);
			if (CollectionUtils.isNotEmpty(cacheSearchList)) {
				List<?> searchList = cacheSearchList.stream()
						.map(data -> JSONUtil.toBean(data, Map.class))
						.collect(Collectors.toList());
				searchVO.setDataList(searchList);
				searchVO.setTotal(searchList.size());
				log.info("Redis中查询数据成功");
				return searchVO;
			}
		}

		log.info("Redis中没查询到数据,开始从Es中查询数据...");

		// 查es
		SearchQueryEsRequest searchQueryEsRequest = BeanUtil.copyProperties(queryRequest, SearchQueryEsRequest.class, "type");
		searchQueryEsRequest.setType(enumByValue.getType());
		List<AggregatedSearchVO> aggregatedSearchVOS = aggregatedSearchService.aggregatedSearchEs(searchQueryEsRequest);
		if(!CollectionUtil.isEmpty(aggregatedSearchVOS)){
			//如果es有的话,那么拿到id集合再去数据库里查询
			// 根据不同的类型，去查对应的数据库
			log.info("Es查询{}数据成功,开始从数据库查询{}数据...",type,type);
			ServiceAdapter serviceAdapter = typeServiceRegistry.getServiceByType(type);
			List<Long> ids = aggregatedSearchVOS.stream().map(AggregatedSearchVO::getId).collect(Collectors.toList());
			if(CollectionUtil.isEmpty(ids)){
				log.error("Es查询到的id集合空，{}",ids);
				return searchVO;
			}
			List<?> listFromDataSource = serviceAdapter.searchDataList(ids);
			// 讲searchEsList中的对象title,content赋值给listFromdataSource
			if(CollectionUtil.isEmpty(listFromDataSource)){
				log.error("数据库根据id查询到的{}数据为空:{}",type,listFromDataSource);
				return searchVO;
			}
			log.info("Es查询{}数据成功,数据库查询{}数据成功",type,type);
			// 讲listFromDataSource里的每个对象转化成Map对象再次组成集合
			List<Map<String,Object>> dataSourceMapList = listFromDataSource.stream().map(BeanUtil::beanToMap).collect(Collectors.toList());
			// 将es查询到的数据转化成map
			Map<Long, AggregatedSearchVO> esMap = aggregatedSearchVOS.stream().collect(Collectors.toMap(AggregatedSearchVO::getId, v -> v));
			// 将es查询到的数据和mysql查询到的数据合并
			for (Map<String,Object> o : dataSourceMapList) {
				Long id = (Long) o.get("id");
				if (esMap.containsKey(id)) {
					o.put("title",esMap.get(id).getTitle());
					if(!StringUtil.isBlank(esMap.get(id).getContent()) && !Objects.equals(esMap.get(id).getContent(),null)){
						o.put("content",esMap.get(id).getContent());
					}
				}
			}
			searchVO.setDataList(dataSourceMapList);
			searchVO.setTotal(aggregatedSearchVOS.size());
			//将搜索数据缓存到redis中,交给mq去做
			redisSendMessageToMQ(key,dataSourceMapList,OperationTypeEnum.REDIS.getType());
			log.info("Es和mysql查询数据成功");
			return searchVO;
		}

		log.info("Es和Redis中没查询到数据，开始从mysql中查询数据...");

		// 查数据库
		DataSource dataSourceByType = dataSourceRegistry.getDataSourceByType(type);
		List<?>  listFromDataSource = dataSourceByType.searchListByTitle(searchText);

		if (!CollectionUtil.isEmpty(listFromDataSource)){
			log.info("mysql中查询数据成功");
			searchVO.setDataList(listFromDataSource);
			searchVO.setTotal(listFromDataSource.size());
			redisSendMessageToMQ(key,listFromDataSource,OperationTypeEnum.REDIS.getType());
			return searchVO;
		}

		// 爬取数据
		log.info("Es,Redis,Mysql都没查询到数据,开始爬取数据...");
		List<?> dataList = dataSourceByType.doSearch(searchText, current, size);
		searchVO.setTotal(dataList.size());
		searchVO.setDataList(dataList);
		log.info("爬取数据数据成功");
		redisSendMessageToMQ(key,dataList,OperationTypeEnum.REDIS.getType());
		return searchVO;
//		//线程池
//		//爬取数据并入库
//		CompletableFuture<? extends List<?>> esSearchTask = CompletableFuture.supplyAsync(() -> {
//			// 爬取数据
//			return dataSourceByType.doSearch(searchText, current, size);
//		}, esSearchExecutor).exceptionally((e) ->{
//			 log.error("数据入库失败:{}",e.getMessage());
//			return new ArrayList<>();
//		});
//		List<?> searchFromDataSources = esSearchTask.join();
	}

	public void saveSearchList(String key,List<?> dataSourceMapList){
		ListOperations<String, String> list = stringRedisTemplate.opsForList();
		if (CollectionUtils.isNotEmpty(dataSourceMapList)) {
			log.info("开始同步数据到Redis...");
			List<String> dataList = dataSourceMapList.stream().map(JSONUtil::toJsonStr).collect(Collectors.toList());
			list.rightPushAll(key, dataList);
			stringRedisTemplate.expire(key, RedisConstant.SEARCH_TTL, TimeUnit.MINUTES);
		}
		log.info("同步数据到Redis结束");
	}

	private void redisSendMessageToMQ(String key,List<?> dataSourceMapList,Integer flag){
		 log.info("Redis的key为{},消息发送中...",key);
		 InsertBatchDTO insertBatchDTO = new InsertBatchDTO();
		 insertBatchDTO.setDataList(dataSourceMapList);
		 insertBatchDTO.setKey(key);
		 insertBatchDTO.setFlag(flag);
		 searchMessageProducer.sendMessage(insertBatchDTO);
	}
}
