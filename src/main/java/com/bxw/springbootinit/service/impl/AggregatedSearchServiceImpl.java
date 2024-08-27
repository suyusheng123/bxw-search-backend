package com.bxw.springbootinit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bxw.springbootinit.adapter.service.ServiceAdapter;
import com.bxw.springbootinit.constant.RedisConstant;
import com.bxw.springbootinit.model.dto.mq.InsertBatchDTO;
import com.bxw.springbootinit.model.dto.query.QueryRequest;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bxw.springbootinit.common.ErrorCode;
import com.bxw.springbootinit.constant.CommonConstant;
import com.bxw.springbootinit.exception.BusinessException;
import com.bxw.springbootinit.mapper.AggregatedSearchMapper;
import com.bxw.springbootinit.model.dto.query.AggregatedSearchEsDTO;
import com.bxw.springbootinit.model.dto.query.SearchQueryEsRequest;
import com.bxw.springbootinit.model.entity.*;
import com.bxw.springbootinit.model.enums.OperationTypeEnum;
import com.bxw.springbootinit.model.enums.SearchEnum;
import com.bxw.springbootinit.model.enums.SearchTypeEnum;
import com.bxw.springbootinit.model.vo.*;
import com.bxw.springbootinit.mq.SearchMessageProducer;
import com.bxw.springbootinit.registry.service.TypeServiceRegistry;
import com.bxw.springbootinit.service.*;
import com.bxw.springbootinit.utils.MessageIdGnerator;
import com.bxw.springbootinit.utils.SqlUtils;
import com.sun.org.apache.bcel.internal.generic.NEW;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.suggest.response.CompletionSuggestion;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ClassName: AggregatedSearchServiceImpl
 * Description:
 *
 * @Author 坤坤学🐸
 * @Create 2024/8/19 10:22
 * @Version 1.0
 */
@Service
@Slf4j
public class AggregatedSearchServiceImpl extends ServiceImpl<AggregatedSearchMapper, AggregatedSearch> implements AggregatedSearchService {

	@Resource
	private PlatformTransactionManager transactionManager;

	private TransactionDefinition transactionDefinition;
	@Resource
	private ElasticsearchRestTemplate elasticsearchRestTemplate;

	@Resource
	private SearchMessageProducer messageProducer;


	@Resource
	private TypeServiceRegistry typeServiceRegistry;

	@Resource
	private RedissonClient redissonClient;


	@Resource
	private RedisOperationService redisOperationService;


	@PostConstruct
	public void init() {
		this.transactionDefinition = new DefaultTransactionDefinition();
	}

	/**
	 * es搜索
	 *
	 * @param search
	 * @return
	 */
	@Override
	public SearchVO aggregatedSearchEs(SearchQueryEsRequest search) {
		Integer type = search.getType();
		String searchText = search.getSearchText();
		String sortField = search.getSortField();
		String sortOrder = search.getSortOrder();
		if (Boolean.FALSE.equals(SqlUtils.validSortField(sortField))) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		long current = search.getCurrent() - 1;
		long pageSize = search.getPageSize();
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		//过滤条件
		//类型条件
		boolQueryBuilder.filter(QueryBuilders.termQuery(AggregatedSearch.TYPE, type));
//        boolQueryBuilder.filter(QueryBuilders.termQuery("_score", 0.0));
		//关键字检索
		if (StringUtils.isNotBlank(searchText)) {
			boolQueryBuilder.should(QueryBuilders.matchQuery(AggregatedSearch.TITLE, searchText));
			boolQueryBuilder.should(QueryBuilders.matchQuery(AggregatedSearch.CONTENT, searchText));
			boolQueryBuilder.minimumShouldMatch(1);//满足一条就可查询
		}

		//关键词高亮
		HighlightBuilder highlightBuilder = new HighlightBuilder();
		// 假如是图片类型不做高亮
		if (!Objects.equals(type, SearchTypeEnum.PICTURE.getType())) {
			//设置标签前缀
			highlightBuilder.preTags("<font color='red'>");
			//设置标签后缀
			highlightBuilder.postTags("</font>");
			//设置高亮字段
			highlightBuilder.field(AggregatedSearch.TITLE);
			highlightBuilder.field(AggregatedSearch.CONTENT);
		}
		//排序
		SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
		if (StringUtils.isNotBlank(sortField)) {
			sortBuilder = SortBuilders.fieldSort(sortField);
			sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
		}

		//分页
		PageRequest pageRequest = PageRequest.of((int) current, (int) pageSize);

		//构造查询
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder).withHighlightBuilder(highlightBuilder).withPageable(pageRequest).withSorts(sortBuilder).build();
		SearchHits<AggregatedSearchEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, AggregatedSearchEsDTO.class);
		long totalHits = searchHits.getTotalHits();
		log.info("total = {}", totalHits);
		if (searchHits.hasSearchHits()) {
			List<SearchHit<AggregatedSearchEsDTO>> searchHitList = searchHits.getSearchHits();
			List<AggregatedSearchEsDTO> searchList = new ArrayList<>(searchHitList.size());
			for (SearchHit<AggregatedSearchEsDTO> aggregatedSearch : searchHitList) {
				AggregatedSearchEsDTO searchEsDTO = aggregatedSearch.getContent();
				List<String> highlightContent = aggregatedSearch.getHighlightField(AggregatedSearch.CONTENT);
				if (CollUtil.isNotEmpty(highlightContent)) {
					for (String content : highlightContent) {
						searchEsDTO.setContent(content);
					}
				}
				List<String> highlightTitle = aggregatedSearch.getHighlightField(AggregatedSearch.TITLE);
				if (CollUtil.isNotEmpty(highlightTitle)) {
					for (String title : highlightTitle) {
						searchEsDTO.setTitle(title);
					}
				}
				searchList.add(searchEsDTO);
			}
			List<AggregatedSearchVO> aggregatedSearchVOS = BeanUtil.copyToList(searchList, AggregatedSearchVO.class);
			SearchVO searchVO = new SearchVO();
			searchVO.setTotal(totalHits);
			searchVO.setDataList(aggregatedSearchVOS);
			return searchVO;
		}
		log.info("搜索未命中 searchText = {} type = {}", searchText, type);
		return new SearchVO();
	}


	/**
	 * 搜索建议
	 *
	 * @param keyword 关键词
	 * @return
	 */
	@Override
	public List<SuggestVO> getSearchSuggest(String keyword) {
		// 判断关键词
		if (StringUtils.isBlank(keyword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		SuggestBuilder suggestBuilder = new SuggestBuilder();
		suggestBuilder.addSuggestion("suggestTitle", new CompletionSuggestionBuilder("title.complete").text(keyword));
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().
				withSuggestBuilder(suggestBuilder).build();
		SearchHits<AggregatedSearchEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, AggregatedSearchEsDTO.class);
		Suggest suggest = searchHits.getSuggest();
		if (ObjectUtil.isNotNull(suggest)) {
			List<Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>>> suggestionList
					= suggest.getSuggestions();
			List<AggregatedSearchEsDTO> searchEsDTOList = new ArrayList<>(suggestionList.size());
			for (int i = 0; i < suggestionList.size(); i++) {
				CompletionSuggestion<CompletionSuggestion.Entry<CompletionSuggestion.Entry.Option>> suggestion = (CompletionSuggestion<CompletionSuggestion.Entry<CompletionSuggestion.Entry.Option>>) suggestionList.get(i);
				// 一个entry对应一个suggestion中一个关键词（也就是一个prefix）的搜索结果，只有一个的情况下可以直接取
				List<CompletionSuggestion.Entry<CompletionSuggestion.Entry<CompletionSuggestion.Entry.Option>>> entries = suggestion.getEntries();
				for (CompletionSuggestion.Entry<CompletionSuggestion.Entry<CompletionSuggestion.Entry.Option>> entry : entries) {
					// options保存的是最终的结果
					List<CompletionSuggestion.Entry.Option<CompletionSuggestion.Entry<CompletionSuggestion.Entry.Option>>> options = entry.getOptions();
					if (CollUtil.isEmpty(options)) {
						continue;
					}
					for (CompletionSuggestion.Entry.Option<CompletionSuggestion.Entry<CompletionSuggestion.Entry.Option>> option : options) {
						SearchHit<CompletionSuggestion.Entry<CompletionSuggestion.Entry.Option>> searchHit = option.getSearchHit();
						//float score = searchHit.getScore();
						Object content = searchHit.getContent();
						if (ObjectUtil.isNotNull(content)) {
							AggregatedSearchEsDTO aggregatedSearchEsDTO = BeanUtil.toBean(content, AggregatedSearchEsDTO.class);
							searchEsDTOList.add(aggregatedSearchEsDTO);
						}
					}
				}
			}
			if (CollUtil.isEmpty(searchEsDTOList)) {
				return new ArrayList<>();
			}
			return searchEsDTOList.stream().map(dto -> BeanUtil.toBean(dto, SuggestVO.class)).collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	/**
	 * 爬取文章的数据
	 *
	 * @param current 页码
	 */
	@Override
	public SearchVO fetchArticles(String searchText, long current) {
		log.info("开始爬取文章数据...");
		String url = String.format("https://kaifa.baidu.com/rest/v1/search?wd=elasticsearch&paramList=page_num=%s,page_size=10&pageNum=%s&pageSize=10", current, current);
		if (!StringUtil.isBlank(searchText)) {
			url = String.format("https://kaifa.baidu.com/rest/v1/search?wd=%s&paramList=page_num=%s,page_size=10&pageNum=%s&pageSize=10", searchText, current, current);
		}
		String res = HttpRequest.get(url).execute().body();
		String sourceUrl = URLUtil.url(url).getHost();
		//2、处理数据
		if (StringUtils.isBlank(res)) {
			log.error("查询到的文章数据为空,{}", res);
			return new SearchVO();
		}
		Map<String, Object> dataMap = JSONUtil.toBean(res, Map.class);
		if (CollectionUtil.isEmpty(dataMap)) {
			log.error("文章接口返回的接口内容为空,{}", dataMap);
			return new SearchVO();
		}

		if (!(dataMap.get("status") instanceof String)) {
			log.error("文章数据接口类型异常,类型为{}", dataMap.get("status").getClass());
		}
		String urlStatus = (String) dataMap.get("status");
		if (StringUtil.isBlank(urlStatus)) {
			log.error("文章数据接口状态码异常,{}", urlStatus);
			return new SearchVO();
		}


		if (!(dataMap.get("data") instanceof JSONObject)) {
			log.error("文章接口数据data字段类型异常,类型为{}", dataMap.get("data").getClass());
			return new SearchVO();
		}
		JSONObject data = (JSONObject) dataMap.get("data");
		if (ObjectUtils.isEmpty(data)) {
			log.error("文章接口返回的data字段为空,{}", data);
			return new SearchVO();
		}

		if (!(data.get("documents") instanceof JSONObject)) {
			log.error("文章接口返回的documents字段类型异常,类型为{}", data.get("documents").getClass());
			return new SearchVO();
		}
		JSONObject documents = (JSONObject) data.get("documents");
		if (ObjectUtils.isEmpty(documents)) {
			log.error("文章接口返回的documents字段为空,{}", documents);
			return new SearchVO();
		}

		// 结果总数
		if (!(documents.get("totalCount") instanceof Integer)) {
			log.error("文章接口返回的totalCount字段类型异常,类型为{}", documents.get("totalCount").getClass());
			return new SearchVO();
		}
		Long total = Long.valueOf(documents.getInt("totalCount"));
		if (Objects.equals(total, 0L)) {
			log.info("文章数据为空，total为{}", total);
			return new SearchVO();
		}


		if (!(documents.get("data") instanceof JSONArray)) {
			log.error("文章真正数据类型异常,类型为{}", documents.get("data").getClass());
			return new SearchVO();
		}
		JSONArray records = (JSONArray) documents.get("data");
		if (ObjectUtils.isEmpty(records)) {
			log.error("文章真正数据为空,{}", records);
			return new SearchVO();
		}


		List<AggregatedSearch> searches = new ArrayList<>(records.size());
		List<Article> articles = new ArrayList<>(records.size());
		//处理文章数据
		for (Object itemData : records) {
			// 类型校验
			if (!(itemData instanceof JSONObject)) continue;
			JSONObject item = (JSONObject) itemData;
			if (!(item.get("techDocDigest") instanceof JSONObject)) continue;
			JSONObject newItem = (JSONObject) item.get("techDocDigest");
			if (ObjectUtil.isEmpty(newItem)) continue;
			AggregatedSearch search = new AggregatedSearch();
			Article article = new Article();


			long snowflakeNextId = IdUtil.getSnowflakeNextId();

			//文章标题
			String title = newItem.getStr("title");
			if (StringUtil.isBlank(title) || title.equals("null")) continue;
			String articleTitleId = MessageIdGnerator.generateNumericId(title + SearchTypeEnum.Article.getValue());
			String aggregatedTitleId = MessageIdGnerator.generateNumericId(title + SearchTypeEnum.Article.getValue());
			//内容
			String content = newItem.getStr("summary");
			if (StringUtil.isBlank(content) || content.equals("null")) continue;

			// 来源
			String articleUrl = newItem.getStr("url");
			if (StringUtil.isBlank(articleUrl) || articleUrl.equals("null")) continue;

			// 发布时间
			String publishTime = newItem.getStr("publishTime");
			if (StringUtil.isBlank(publishTime) || publishTime.equals("null")) continue;

			log.info("文章数据为 title = {} content = {} url= {} publishTime= {}", title, content, url, publishTime);
			article.setId(snowflakeNextId);
			article.setTitle(doStr(title));
			article.setContent(doStr(content));
			article.setUrl(articleUrl);
			article.setSourceUrl(sourceUrl);
			article.setPublishTime(publishTime);
			article.setArticleTitleId(articleTitleId);


			search.setId(snowflakeNextId);
			search.setTitle(doStr(title));
			search.setContent(doStr(content));
			search.setType(SearchTypeEnum.Article.getType());
			search.setAggregatedTitleId(aggregatedTitleId);


			articles.add(article);
			searches.add(search);
		}
		// 对articles进行排序,发布时间排序
		articles.sort((o1, o2) -> {
			if (o1.getPublishTime() == null) {
				return 1;
			}
			if (o2.getPublishTime() == null) {
				return -1;
			}
			return o2.getPublishTime().compareTo(o1.getPublishTime());
		});
		// 再将articles转化为articleVOs
		List<ArticleVO> articleVOS = articles.stream().map(article -> BeanUtil.toBean(article, ArticleVO.class)).collect(Collectors.toList());
		SearchVO searchVO = new SearchVO();
		searchVO.setTotal(total);
		searchVO.setDataList(articleVOS);
		String messageLockKey = RedisConstant.MESSAGE_LOCK_KEY + SearchTypeEnum.Article.getValue() + ":" + searchText + ":" + current;
		String messageKey = RedisConstant.MESSAGE_KEY + SearchTypeEnum.Article.getValue() + ":" + searchText + ":" + current;
		log.info("文章数据爬取成功,开始加锁发送消息,消息key为{}", messageLockKey);
		RLock messageLock = redissonClient.getLock(messageLockKey);
		boolean isMessageLock = messageLock.tryLock();
		if (isMessageLock) {
			try {
				log.info("{}线程加锁成功,开始判断消息是否被消费过了...", Thread.currentThread().getName());
				// 先判断这则消息是否已经被消费或者正在消费中,0表示未消费或者消费失败,1表示消费成功或者正在消费
				String messageStatus = redisOperationService.getMessageStatus(messageKey);
				if (Objects.equals(messageStatus, "0")) {
					//发送消息
					sendMessageToMq(searches, articles, SearchTypeEnum.Article.getValue(), OperationTypeEnum.MYSQL.getType(), messageKey);
					redisOperationService.setMessageStatus(messageKey, "1");
				} else {
					log.info("文章消息已经消费过了,消息状态为{}", messageStatus);
				}
			} catch (Exception e) {
				log.error("消息{}发送异常,异常为{}", messageKey, e);
				redisOperationService.setMessageStatus(messageKey, "0");
				return searchVO;
			} finally {
				log.info("线程{}释放锁,锁的key为{}", Thread.currentThread().getName(), messageLockKey);
				messageLock.unlock();
			}
		}
		return searchVO;
	}

	/**
	 * 爬取图片数据
	 *
	 * @param searchText
	 * @param first
	 */
	@Override
	public SearchVO fetchPictures(String searchText, long first) {
		log.info("开始爬取图片数据...");
		String url = String.format("https://cn.bing.com/images/async?q=世界旅游胜地first=%s&mmasync=1", first);
		if (!StringUtil.isBlank(searchText)) {
			url = String.format("https://cn.bing.com/images/async?q=%s&first=%s&mmasync=1", searchText, first);
		}
		String sourceUrl = URLUtil.url(url).getHost();
		Document bingDoc = null;
		try {
			bingDoc = Jsoup.connect(url).get();
		} catch (IOException e) {
			log.error("爬取图片数据失败:{}", e.getMessage());
			throw new RuntimeException(e);
		}
		//获取 图片列表 iuscp isv 元素
		Elements elements = bingDoc.select(".iuscp.isv");
		if (CollUtil.isEmpty(elements)) {
			log.error("图片html没有 .iuscp.isv");
			return new SearchVO();
		}
		List<AggregatedSearch> searches = new ArrayList<>(10);
		List<Picture> pictures = new ArrayList<>(10);
		for (Element element : elements) {
			if (searches.size() >= 10 && pictures.size() >= 10) {
				break;
			}

			AggregatedSearch search = new AggregatedSearch();
			Picture picture = new Picture();

			//获取 a标签
			Elements pic = element.select(".iusc");
			if (CollUtil.isEmpty(pic)) {
				log.error("图片没有 class .iusc ");
				continue;
			}

			//m 属性 图片数据：图片url、图片标题
			String m = pic.attr("m");
			if (StringUtils.isBlank(m)) {
				log.error("图片没有m属性");
				continue;
			}

			Map<String, Object> map = JSONUtil.toBean(m, Map.class);

			//获取图片url
			// 判断类型
			if (!(map.get("turl") instanceof String)) {
				log.error("图片类型turl异常,异常为{}", map.get("turl").getClass());
				continue;
			}
			String picUrl = (String) map.get("turl");
			if (StringUtil.isBlank(picUrl) || picUrl.equals("null")) continue;
			Elements inflnk = element.select(".inflnk");

			//获取标题
			String picTitle = inflnk.attr("aria-label");
			if (StringUtil.isBlank(picTitle) || picTitle.equals("null")) continue;
			String pictureTitleId = MessageIdGnerator.generateNumericId(picTitle + SearchTypeEnum.PICTURE.getValue());
			String aggregatedTitleId = MessageIdGnerator.generateNumericId(picTitle + SearchTypeEnum.PICTURE.getValue());
			long snowflakeNextId = IdUtil.getSnowflakeNextId();

			picture.setId(snowflakeNextId);
			picture.setUrl(picUrl);
			picture.setTitle(doStr(picTitle));
			picture.setSourceUrl(sourceUrl);
			picture.setPictureTitleId(pictureTitleId);

			search.setId(snowflakeNextId);
			search.setTitle(doStr(picTitle));
			search.setType(SearchTypeEnum.PICTURE.getType());
			search.setAggregatedTitleId(aggregatedTitleId);


			log.info("图片数据为 url = {} title = {} source = {}", picUrl, picTitle, sourceUrl);
			pictures.add(picture);
			searches.add(search);
		}
		// 再将pictures转化为pictureVOs
		List<PictureVO> pictureVOS = pictures.stream().map(picture -> BeanUtil.toBean(picture, PictureVO.class)).collect(Collectors.toList());
		SearchVO searchVO = new SearchVO();
		searchVO.setDataList(pictureVOS);
		searchVO.setTotal(pictureVOS.size());
		long current = (first - 35) / 10 + 1;
		String messageLockKey = RedisConstant.MESSAGE_LOCK_KEY + SearchTypeEnum.PICTURE.getValue() + ":" + searchText + ":" + current;
		String messageKey = RedisConstant.MESSAGE_KEY + SearchTypeEnum.PICTURE.getValue() + ":" + searchText + ":" + current;
		log.info("图片数据爬取成功,开始加锁发送消息,消息key为{}", messageLockKey);
		RLock messageLock = redissonClient.getLock(messageLockKey);
		boolean isMessageLock = messageLock.tryLock();
		if (isMessageLock) {
			try {
				log.info("{}线程加锁成功,开始判断消息是否被消费过了...", Thread.currentThread().getName());
				// 先判断这则消息是否已经被消费或者正在消费中,0表示未消费或者消费失败,1表示正在消费,2表示消费成功
				String messageStatus = redisOperationService.getMessageStatus(messageKey);
				if (Objects.equals(messageStatus, "0")) {
					//发送消息
					sendMessageToMq(searches, pictures, SearchTypeEnum.PICTURE.getValue(), OperationTypeEnum.MYSQL.getType(), messageKey);
					redisOperationService.setMessageStatus(messageKey, "1");
				} else {
					log.info("图片消息已经消费过了,消息状态为{}", messageStatus);
				}
			} catch (Exception e) {
				log.error("消息{}发送异常,异常为{}", messageKey, e);
				redisOperationService.setMessageStatus(messageKey, "0");
				return searchVO;
			} finally {
				log.info("线程{}释放锁,锁的key为{}", Thread.currentThread().getName(), messageLockKey);
				messageLock.unlock();
			}
		}
		return searchVO;
	}

	/**
	 * 爬取视频数据
	 *
	 * @param searchText
	 * @param first
	 */
	@Override
	public SearchVO fetchVideos(String searchText, long first) {
		log.info("开始爬取视频数据...");
		String url = String.format("https://kaifa.baidu.com/rest/v1/search?wd=elasticsearch&paramList=page_num=%s,page_size=12,resource_type=VIDEO&pageNum=%s&pageSize=10", first, first);
		if (!StringUtil.isBlank(searchText)) {
			url = String.format("https://kaifa.baidu.com/rest/v1/search?wd=%s&paramList=page_num=%s,page_size=12,resource_type=VIDEO&pageNum=%s&pageSize=10", searchText, first, first);
		}
		String res = HttpRequest.get(url).execute().body();
		String sourceUrl = URLUtil.url(url).getHost();
		//2、处理数据
		if (StringUtils.isBlank(res)) {
			log.error("爬取视频数据失败:{}", res);
			return new SearchVO();
		}
		Map<String, Object> dataMap = JSONUtil.toBean(res, Map.class);
		if (CollectionUtil.isEmpty(dataMap)) {
			log.error("爬取视频数据接口返回的接口内容为空:{}", dataMap);
			return new SearchVO();
		}

		if (!(dataMap.get("status") instanceof String)) {
			log.error("爬取视频数据接口status字段类型异常:{}", dataMap.get("status").getClass());
		}
		String urlStatus = (String) dataMap.get("status");
		if (StringUtil.isBlank(urlStatus)) {
			log.error("爬取视频数据第三方接口状态码异常:{}", urlStatus);
			return new SearchVO();
		}


		if (!(dataMap.get("data") instanceof JSONObject)) {
			log.error("爬取视频数据接口data字段类型异常:{}", dataMap.get("data").getClass());
			return new SearchVO();
		}
		JSONObject data = (JSONObject) dataMap.get("data");
		if (ObjectUtils.isEmpty(data)) {
			log.error("爬取到的视频data字段为空:{}", data);
			return new SearchVO();
		}

		if (!(data.get("documents") instanceof JSONObject)) {
			log.error("爬取到的视频documents字段类型异常:{}", data.get("documents").getClass());
			return new SearchVO();
		}
		JSONObject documents = (JSONObject) data.get("documents");
		if (ObjectUtils.isEmpty(documents)) {
			log.error("爬取到的视频documents字段为空:{}", documents);
			return new SearchVO();
		}

		// 结果总数
		if (!(documents.get("totalCount") instanceof Integer)) {
			log.error("爬取到的视频totalCount字段类型异常:{}", documents.get("totalCount").getClass());
			return new SearchVO();
		}
		Long total = Long.valueOf(documents.getInt("totalCount"));
		if (Objects.equals(total, 0L)) {
			log.info("文章数据为空，total为{}", total);
			return new SearchVO();
		}


		if (!(documents.get("data") instanceof JSONArray)) {
			log.error("爬取到的视频data字段类型异常:{}", documents.get("data").getClass());
			return new SearchVO();
		}
		JSONArray records = (JSONArray) documents.get("data");
		if (ObjectUtils.isEmpty(records)) {
			log.error("爬取到的真正视频数据为空:{}", records);
			return new SearchVO();
		}

		List<AggregatedSearch> searches = new ArrayList<>(records.size());
		List<Video> videos = new ArrayList<>(records.size());
		//处理视频数据
		for (Object itemData : records) {
			// 类型校验
			if (!(itemData instanceof JSONObject)) continue;
			JSONObject item = (JSONObject) itemData;
			if (ObjectUtil.isEmpty(item)) continue;
			if (!(item.get("techDocDigest") instanceof JSONObject)) continue;
			JSONObject newItem = (JSONObject) item.get("techDocDigest");
			if (ObjectUtil.isEmpty(newItem)) continue;
			AggregatedSearch search = new AggregatedSearch();
			Video video = new Video();

			//视频标题
			String title = newItem.getStr("title");
			if (StringUtil.isBlank(title) || title.equals("null")) continue;
			String videoTitleId = MessageIdGnerator.generateNumericId(title + SearchTypeEnum.VIDEO.getValue());
			String aggregatedTitleId = MessageIdGnerator.generateNumericId(title + SearchTypeEnum.VIDEO.getValue());
			// url
			String videoUrl = newItem.getStr("url");
			if (StringUtil.isBlank(videoUrl) || videoUrl.equals("null")) continue;

			//封面
			String keywords = newItem.getStr("subKeywords");
			if (ObjectUtil.isEmpty(keywords)) continue;
			List<String> keyList = JSONUtil.toList(keywords, String.class);
			Map<String, Object> keyMap = JSONUtil.toBean(keyList.get(0), Map.class);
			if (!(keyMap.get("value") instanceof String)) continue;
			String cover = (String) keyMap.get("value");
			log.info("视频数据为 url = {} title = {} cover= {}", videoUrl, title, cover);

			long snowflakeNextId = IdUtil.getSnowflakeNextId();

			video.setId(snowflakeNextId);
			video.setUrl(videoUrl);
			video.setTitle(doStr(title));
			video.setCover(cover);
			video.setSourceUrl(sourceUrl);
			video.setVideoTitleId(videoTitleId);


			search.setId(snowflakeNextId);
			search.setTitle(doStr(title));
			search.setType(SearchTypeEnum.VIDEO.getType());
			search.setAggregatedTitleId(aggregatedTitleId);

			videos.add(video);
			searches.add(search);
		}
		SearchVO searchVO = new SearchVO();
		// 再将videos转化为videoVOs
		List<VideoVO> videoVOS = videos.stream().map(video -> BeanUtil.toBean(video, VideoVO.class)).collect(Collectors.toList());
		searchVO.setDataList(videoVOS);
		searchVO.setTotal(total);
		String messageLockKey = RedisConstant.MESSAGE_LOCK_KEY + SearchTypeEnum.VIDEO.getValue() + ":" + searchText + ":" + first;
		String messageKey = RedisConstant.MESSAGE_KEY + SearchTypeEnum.VIDEO.getValue() + ":" + searchText + ":" + first;
		log.info("视频数据爬取成功,开始加锁发送消息,消息key为{}", messageLockKey);
		RLock messageLock = redissonClient.getLock(messageLockKey);
		boolean isMessageLock = messageLock.tryLock();
		if (isMessageLock) {
			try {
				log.info("{}线程加锁成功,开始判断消息是否被消费过了...", Thread.currentThread().getName());
				// 先判断这则消息是否已经被消费或者正在消费中,0表示未消费或者消费失败,1表示正在消费,2表示消费成功
				String messageStatus = redisOperationService.getMessageStatus(messageKey);
				if (Objects.equals(messageStatus, "0")) {
					sendMessageToMq(searches, videos, SearchTypeEnum.VIDEO.getValue(), OperationTypeEnum.MYSQL.getType(), messageKey);
					redisOperationService.setMessageStatus(messageKey, "1");
					//发送消息
				} else {
					log.info("视频消息已经消费过了,消息状态为{}", messageStatus);
				}
			} catch (Exception e) {
				log.error("消息{}发送异常,异常为{}", messageKey, e);
				redisOperationService.setMessageStatus(messageKey, "0");
				return searchVO;
			} finally {
				log.info("线程{}释放锁,锁的key为{}", Thread.currentThread().getName(), messageLockKey);
				messageLock.unlock();
			}
		}
		return searchVO;
	}

	@Override
	public void saveSearchTextAndCrawlerData(List<AggregatedSearch> searches, List<?> dataList, String type) {
		// 入库,使用编程式事务
		TransactionStatus status = transactionManager.getTransaction(transactionDefinition);
		try {
			// 开始数据库操作
			log.info("爬取{}数据成功，开始入库...", type);
			log.info("{}数据正在入aggregated_search表中...", type);
			baseMapper.saveAggregatedSearchList(searches);
			log.info("{}数据入aggregated_search表成功", type);
			log.info("{}数据正在入{}表中...", type, type);
			ServiceAdapter serviceByType = typeServiceRegistry.getServiceByType(type);
			if (serviceByType.insertBatchDataList(dataList)) {
				// 提交事务
				transactionManager.commit(status);
				log.info("{}数据入{}表成功", type, Objects.requireNonNull(SearchTypeEnum.getEnumByValue(type)).getText());
				log.info("{}数据入库成功", type);
			} else {
				// 如果发生异常，则回滚事务
				transactionManager.rollback(status);
				log.error("{}数据入库出现异常,不是抛出异常", type);
			}
		} catch (RuntimeException e) {
			// 如果发生异常，则回滚事务
			transactionManager.rollback(status);
			log.error("{}数据出现异常为:{}", type, e);
		}
	}


	/**
	 * 解析 markdown 文档转为文本
	 *
	 * @param content 文本
	 * @return 文本
	 */
	private String doStr(String content) {
		if (StringUtils.isBlank(content)) {
			return "";
		}
		String replaceStr = content.replaceAll("!\\[.*?\\]\\((.*?)\\)", "");
		Parser parser = Parser.builder().build();
		Node document = parser.parse(replaceStr);
		TextContentRenderer renderer = TextContentRenderer.builder().build();
		String render = renderer.render(document);
		return render.length() > 200 ? render.substring(0, 200) : render;
	}

	private void sendMessageToMq(List<AggregatedSearch> searches, List<?> dataList, String type, int flag, String messageKey) {
		log.info("{}类型消息发送中...", type);
		InsertBatchDTO insertBatchDTO = new InsertBatchDTO();
		insertBatchDTO.setSearches(searches);
		insertBatchDTO.setDataList(dataList);
		insertBatchDTO.setType(type);
		insertBatchDTO.setMessageKey(messageKey);
		messageProducer.sendMessage(insertBatchDTO);
	}


}
