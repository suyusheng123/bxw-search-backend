package com.bxw.springbootinit.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.bxw.springbootinit.adapter.method.aggregatedservice.FetchDataMethodAdapter;
import com.bxw.springbootinit.adapter.service.ServiceAdapter;
import com.bxw.springbootinit.common.ErrorCode;
import com.bxw.springbootinit.constant.RedisConstant;
import com.bxw.springbootinit.exception.BusinessException;
import com.bxw.springbootinit.model.dto.query.QueryRequest;
import com.bxw.springbootinit.model.dto.query.SearchQueryEsRequest;
import com.bxw.springbootinit.model.enums.SearchTypeEnum;
import com.bxw.springbootinit.model.vo.AggregatedSearchVO;
import com.bxw.springbootinit.model.vo.SearchVO;
import com.bxw.springbootinit.model.vo.SuggestVO;
import com.bxw.springbootinit.registry.method.aggregatedservice.FetchDataMethodRegistry;
import com.bxw.springbootinit.registry.service.TypeServiceRegistry;
import com.bxw.springbootinit.service.AggregatedSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.internal.StringUtil;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ClassName:SearchFacade
 * Package:com.bxw.springbootinit.manager
 * Description:
 * 搜索门面模式
 * @Author 卜翔威
 * @Create 2024/8/14 8:43
 * @Version 1.0
 */

@Component
@Slf4j
public class SearchFacade {

	@Resource
	private RedissonClient redissonClient;


	@Resource
	private TypeServiceRegistry typeServiceRegistry;


	@Resource
	private StringRedisTemplate stringRedisTemplate;

	@Resource
	private AggregatedSearchService aggregatedSearchService;

	@Resource
	private FetchDataMethodRegistry fetchDataMethodRegistry;


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
	 * 聚合搜索升级版最初(redis抗压 + 线程池爬取数据到mysql+ mysql同步es + es高亮 + 数据组合)
	 * 聚合搜索升级版之后(redis抗压 + 爬取数据时采用读写锁的方式)
	 * @param queryRequest
	 * @return
	 */
	public void searchAll(QueryRequest queryRequest, SearchVO searchVO) {
		String type = queryRequest.getType();
		if (StringUtils.isBlank(type)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		SearchTypeEnum enumByValue = SearchTypeEnum.getEnumByValue(type);
		if (enumByValue == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		String searchText = queryRequest.getSearchText();
		if (StringUtil.isBlank(searchText) || Objects.equals(searchText, "null")) {
			searchText = "c++";
		}
		// 如果前端传入的type为null或者是空字符串,那么就给他一个默认值
		long current = queryRequest.getCurrent();
		long size = queryRequest.getPageSize();
		SearchVO searchByEsVO = new SearchVO();
		SearchVO searchByFetchVO = new SearchVO();
		String key = RedisConstant.SEARCH_KEY + enumByValue.getValue() + ":" + searchText + ":" + current;
		String searchLockKey = RedisConstant.SEARCH_LOCK_KEY + enumByValue.getValue() + ":" + searchText + ":" + current;
		log.info("开始从Redis中查询数据...");
		//从redis中获取搜索数据,考虑到分页的情况,存储在Redis的key对应value类型调整为String类型
		if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
			String cacheSearchJson = stringRedisTemplate.opsForValue().get(key);
			Map<String, Object> cacheSearchMap = JSONUtil.toBean(cacheSearchJson, Map.class);
			if (CollectionUtil.isNotEmpty(cacheSearchMap)) {
				// 先获取map中的current字段,判断是不是我当前页的数据
				int redisCurrent = (int) cacheSearchMap.get("current");
				if (Objects.equals((long) redisCurrent, current)) {
					log.info("Redis中查询数据成功，key为{}，第{}页", key, current);
					JSONArray dataList = (JSONArray) cacheSearchMap.get("dataList");
					List<?> searchList = dataList.stream()
							.map(data -> BeanUtil.toBean(data, Map.class))
							.collect(Collectors.toList());
					searchVO.setDataList(searchList);
					int total = (int) cacheSearchMap.get("total");
					searchVO.setTotal((long) total);
					searchVO.setCurrent(current);
					log.info("Redis中查询数据成功");
					return;
				}
			}
		}
		log.info("Redis中没查询到第{}页数据,开始从Es中查询数据...", current);
		// 查es
		SearchQueryEsRequest searchQueryEsRequest = BeanUtil.copyProperties(queryRequest, SearchQueryEsRequest.class, "type");
		searchQueryEsRequest.setType(enumByValue.getType());
		SearchVO searchEs = aggregatedSearchService.aggregatedSearchEs(searchQueryEsRequest);
		// 将searchVO的dataList转化成aggregatedSearchVOS
		List<?> esDataList = searchEs.getDataList();
		if (!CollectionUtil.isEmpty(esDataList)) {
			// 将seachEs里的total取出来
			long esTotal = searchEs.getTotal();
			List<AggregatedSearchVO> aggregatedSearchVOS = esDataList.stream()
					.map(data -> BeanUtil.toBean(data, AggregatedSearchVO.class))
					.collect(Collectors.toList());
			if (!CollectionUtil.isEmpty(aggregatedSearchVOS)) {
				//如果es有的话,那么拿到id集合再去数据库里查询
				// 根据不同的类型，去查对应的数据库
				log.info("Es查询{}数据成功,开始从数据库查询{}数据...", type, type);
				ServiceAdapter serviceAdapter = typeServiceRegistry.getServiceByType(type);
				List<Long> ids = aggregatedSearchVOS.stream().map(AggregatedSearchVO::getId).collect(Collectors.toList());
				if (CollectionUtil.isEmpty(ids)) {
					log.info("Es查询到的id集合空，{}", ids);
					return;
				}
				List<?> listFromDataSource = serviceAdapter.searchDataList(ids);
				// 讲searchEsList中的对象title,content赋值给listFromDataSource
				if (CollectionUtil.isEmpty(listFromDataSource)) {
					log.error("数据库根据id查询到的{}数据为空:{}", type, listFromDataSource);
					return;
				}
				log.info("Es查询{}数据成功,数据库查询{}数据成功", type, type);
				// 讲listFromDataSource里的每个对象转化成Map对象再次组成集合
				List<Map<String, Object>> dataSourceMapList = listFromDataSource.stream().map(BeanUtil::beanToMap).collect(Collectors.toList());
				// 将es查询到的数据转化成map
				Map<Long, AggregatedSearchVO> esMap = aggregatedSearchVOS.stream().collect(Collectors.toMap(AggregatedSearchVO::getId, v -> v));
				// 将es查询到的数据和mysql查询到的数据合并
				for (Map<String, Object> o : dataSourceMapList) {
					Long id = (Long) o.get("id");
					if (esMap.containsKey(id)) {
						o.put("title", esMap.get(id).getTitle());
						if (!StringUtil.isBlank(esMap.get(id).getContent()) && !Objects.equals(esMap.get(id).getContent(), null)) {
							o.put("content", esMap.get(id).getContent());
						}
					}
				}
				log.info("Es和mysql查询数据成功");
				searchByEsVO.setDataList(dataSourceMapList);
				searchByEsVO.setTotal(esTotal);
				searchByEsVO.setCurrent(current);
				searchByEsVO.setIsHighlight(1);
				// 判断从es查询到的数据总数是否大于当前页面所要展示的条数
				if (esTotal > current * size) {
					List<?> dataList = searchByEsVO.getDataList();
					long total = searchByEsVO.getTotal();
					searchVO.setTotal(total);
					searchVO.setDataList(dataList);
					searchVO.setCurrent(current);
					searchVO.setIsHighlight(1);
					// 将searchVO对象转化为json串
					// 将搜索数据缓存到redis中
					String esJson = JSONUtil.toJsonStr(searchVO);
					saveSearchList(key, esJson);
					log.info("Es查询到的数据总数total:{}大于current * size:{}可以继续分页,key为{}直接返回", total, current * size, key);
					return;
				}
				log.info("Es查询到的数据总数total:{}小于current*size:{}不足以继续分页,key为{}开始爬虫...", esTotal, current * size, key);
			}
		}

//		RLock searchLock = redissonClient.getLock(searchLockKey);
		// Step 1: 获取 Redisson 读写锁
		RReadWriteLock rwLock = redissonClient.getReadWriteLock(searchLockKey);
		// 获取写锁
		RLock wLock = rwLock.writeLock();
		// 利用Redisson进行加锁;
		boolean tryWLock = false;
		tryWLock = wLock.tryLock();
		if (tryWLock) {
			try {
				log.info("线程{}加写锁成功,key为{}", Thread.currentThread().getName(), searchLockKey);
				// 爬取数据
				log.info("Es,Redis没查询到第{}页数据,key为{}开始爬取数据...", current, key);
				FetchDataMethodAdapter methodByType = fetchDataMethodRegistry.getMethodByType(type);
				SearchVO fetchSearchVO = methodByType.fetchData(searchText, current);
				// 判断第三方网站有没有爬到数据
				if (!CollectionUtil.isEmpty(fetchSearchVO.getDataList())) {
					searchByFetchVO.setTotal(fetchSearchVO.getTotal());
					searchByFetchVO.setDataList(fetchSearchVO.getDataList());
					searchByFetchVO.setCurrent(current);
					if (fetchSearchVO.getTotal() > current * size) {
						log.info("爬取数据数据成功");
						List<?> dataList = searchByFetchVO.getDataList();
						long total = searchByFetchVO.getTotal();
						//将搜索数据缓存到redis中
						searchVO.setTotal(total);
						searchVO.setDataList(dataList);
						searchVO.setCurrent(current);
						// 将searchVO对象转化为json串
						//将搜索数据缓存到redis中
						saveSearchList(key, JSONUtil.toJsonStr(searchVO));
						log.info("爬取数据查询到的数据总数total:{}大于current*size:{}可以继续分页,key为{}", fetchSearchVO.getTotal(), current * size, key);
						return;
					}
					log.info("爬取数据查询到的数据总数total:{}小于current*size{},key为{}", fetchSearchVO.getTotal(), current * size, key);
				}
				// 判断哪个VO的total不为0
				if (searchByEsVO.getTotal() != 0) {
					List<?> dataList = searchByEsVO.getDataList();
					long total = searchByEsVO.getTotal();
					searchVO.setTotal(total);
					searchVO.setDataList(dataList);
					searchVO.setCurrent(current);
					searchVO.setIsHighlight(1);
				} else if (searchByFetchVO.getTotal() != 0) {
					List<?> dataList = searchByFetchVO.getDataList();
					long total = searchByFetchVO.getTotal();
					searchVO.setTotal(total);
					searchVO.setDataList(dataList);
					searchVO.setCurrent(current);
				}
				// 将searchVO对象转化为json串
				// 将搜索数据缓存到redis中
				String searchJson = JSONUtil.toJsonStr(searchVO);
				saveSearchList(key, searchJson);
			} catch (Exception e) {
				log.error("线程{},key为{},异常为:{}", Thread.currentThread().getName(), key, e);
			} finally {
				log.info("线程{}释放锁,key为{}", Thread.currentThread().getName(), searchLockKey);
				wLock.unlock();
			}
		} else {
			RLock rLock = rwLock.readLock();
			boolean tryRLock = false;
			try {
				tryRLock = rLock.tryLock(6, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				log.error("加锁异常,异常为{}", e);
			}
			if (tryRLock) {
				try {
					log.info("线程{}加读锁成功，key为{}", Thread.currentThread().getName(), searchLockKey);
					log.info("开始从Redis中查询数据...");
					//从redis中获取搜索数据,考虑到分页的情况,存储在Redis的key对应value类型调整为String类型
					if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
						String cacheSearchJson = stringRedisTemplate.opsForValue().get(key);
						Map<String, Object> cacheSearchMap = JSONUtil.toBean(cacheSearchJson, Map.class);
						if (CollectionUtil.isNotEmpty(cacheSearchMap)) {
							// 先获取map中的current字段,判断是不是我当前页的数据
							int redisCurrent = (int) cacheSearchMap.get("current");
							if (Objects.equals((long) redisCurrent, current)) {
								log.info("Redis中查询数据成功，key为{}，第{}页", key, current);
								JSONArray dataList = (JSONArray) cacheSearchMap.get("dataList");
								List<?> searchList = dataList.stream()
										.map(data -> BeanUtil.toBean(data, Map.class))
										.collect(Collectors.toList());
								searchVO.setDataList(searchList);
								int total = (int) cacheSearchMap.get("total");
								searchVO.setTotal((long) total);
								searchVO.setCurrent(current);
								log.info("Redis中查询数据成功");
								return;
							}
						}
					}
				} catch (Exception e) {
					log.error("线程{},key为{},异常为:{}", Thread.currentThread().getName(), key, e);
				} finally {
					log.info("线程{}释放读锁,key为{}", Thread.currentThread().getName(), searchLockKey);
					rLock.unlock();
				}
			}
		}
	}

	public void saveSearchList(String key, String value) {
		// 开始同步数据到Redis
		log.info("开始同步数据到Redis...");
		stringRedisTemplate.opsForValue().set(key, value);
		stringRedisTemplate.expire(key, RedisConstant.SEARCH_TTL, TimeUnit.MINUTES);
		log.info("同步数据到Redis结束");
	}

	public List<String> getSuggestSeachList(String keyword) {
		// 加锁,让请求幂等
		String suggestLockKey = RedisConstant.SUGGEST_LOCK_KEY + keyword;
		RLock suggestLock = redissonClient.getLock(suggestLockKey);
		String suggestKey = RedisConstant.SUGGEST_SEARCH_KEY + keyword;
		// 利用Redisson进行加锁;
		boolean tryLock = false;
		try {
			tryLock = suggestLock.tryLock(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("加锁异常,异常为{}", e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统异常");
		}
		if (tryLock) {
			log.info("关键字搜索业务线程{}加锁成功,key为{}", Thread.currentThread().getName(), suggestLockKey);
			try {
				// 先去Redis查询数据
				log.info("开始从Redis里查数据,线程为{},key为{}", Thread.currentThread().getName(), suggestKey);
				//从redis中获取搜索数据,考虑到分页的情况,存储在Redis的key对应value类型调整为String类型
				if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(suggestKey))) {
					String cacheSearchJson = stringRedisTemplate.opsForValue().get(suggestKey);
					List<String> cacheSearchList = JSONUtil.toList(cacheSearchJson, String.class);
					if (CollectionUtil.isNotEmpty(cacheSearchList)) {
						// 有的话，直接将数据返回给前端
						log.info("Redis中查询到搜索关键字,key为{}", suggestKey);
						return cacheSearchList.stream().map(
								String::valueOf).collect(Collectors.toList());
					}
					log.info("redis中没有搜锁关键字,key为{}", suggestKey);
				}

				// 开始从第三方网站去搜索接口
				log.info("关键字搜索从Redis里没有查到数据，开始去Es查询...线程为{},key为{}", Thread.currentThread().getName(), suggestKey);
				String url = String.format("https://kaifa.baidu.com/rest/v1/recommend/suggests?wd=%s", keyword);
				String res = HttpRequest.get(url).execute().body();
				System.out.println(res);
				Map<String, Object> suggestRes = JSONUtil.toBean(res, Map.class);
				String status = String.valueOf(suggestRes.get("status"));
				if (Objects.equals(status, "OK")) {
					JSONArray suggestWords = (JSONArray) suggestRes.get("data");
					if (!ObjectUtil.isEmpty(suggestWords)) {
						List<String> words = JSONUtil.toList(suggestWords, String.class);
						// 截取集合前8条
						words = words.stream().limit(8).collect(Collectors.toList());
						log.info("第三方网站搜索接口返回数据成功，关键字为{}", keyword);
						// 存入Redis
						saveSuggestList(suggestKey, words);
						return words;
					}
				}
				log.info("第三方网站搜索接口返回数据失败，关键字为{},开始从es查询...", keyword);
				// 再去es里去搜索关键字
				List<SuggestVO> searchSuggest = aggregatedSearchService.getSearchSuggest(keyword);
				// 将searchSuggest的title字段提取出来转化成list<String>
				if (CollectionUtil.isNotEmpty(searchSuggest)) {
					log.info("第三方网站搜索接口返回数据成功，关键字为{}", keyword);
					List<String> suggestList = searchSuggest.stream().map(
							SuggestVO::getTitle).collect(Collectors.toList());
					// 存入Redis
					// 截取记录前8条
					suggestList = suggestList.stream().limit(8).collect(Collectors.toList());
					saveSuggestList(suggestKey, suggestList);
					return suggestList;
				}
				log.info("关键字业务从Es查询成功，线程为{},key为{}", Thread.currentThread().getName(), suggestKey);
			} catch (Exception e) {
				log.error("搜索关键字异常，异常为{}", e);
				throw new RuntimeException(e);
			} finally {
				log.info("线程{}释放锁,key为{}", Thread.currentThread().getName(), suggestLockKey);
				suggestLock.unlock();
			}
		}
		return new ArrayList<>();
	}


	public void saveSuggestList(String key, List<String> suggestList) {
		// 开始同步数据到Redis
		log.info("开始同步数据到Redis...");
		//将搜索数据缓存到redis中
		if (CollectionUtils.isNotEmpty(suggestList)) {
			stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(suggestList));
			stringRedisTemplate.expire(key, RedisConstant.SEARCH_TTL, TimeUnit.MINUTES);
		}
		log.info("同步数据到Redis结束");
	}
}
