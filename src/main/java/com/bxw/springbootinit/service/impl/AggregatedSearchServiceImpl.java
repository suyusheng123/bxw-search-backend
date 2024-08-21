package com.bxw.springbootinit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import co.elastic.clients.elasticsearch.sql.QueryRequest;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bxw.springbootinit.common.ErrorCode;
import com.bxw.springbootinit.constant.CommonConstant;
import com.bxw.springbootinit.exception.BusinessException;
import com.bxw.springbootinit.mapper.AggregatedSearchMapper;
import com.bxw.springbootinit.model.dto.query.AggregatedSearchEsDTO;
import com.bxw.springbootinit.model.dto.query.SearchQueryEsRequest;
import com.bxw.springbootinit.model.entity.*;
import com.bxw.springbootinit.model.enums.SearchTypeEnum;
import com.bxw.springbootinit.model.vo.AggregatedSearchVO;
import com.bxw.springbootinit.model.vo.PageResult;
import com.bxw.springbootinit.model.vo.SuggestVO;
import com.bxw.springbootinit.model.vo.UserVO;
import com.bxw.springbootinit.service.*;
import com.bxw.springbootinit.utils.SqlUtils;
import com.sun.org.apache.bcel.internal.generic.NEW;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.suggest.response.CompletionSuggestion;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest;
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
	private UserService userService;

	@Resource
	private ArticleService articleService;

	@Resource
	private PictureService pictureService;

	@Resource
	private VideoService videoService;


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
	public List<AggregatedSearchVO> aggregatedSearchEs(SearchQueryEsRequest search) {
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
		//设置标签前缀
		highlightBuilder.preTags("<font color='red'>");
		//设置标签后缀
		highlightBuilder.postTags("</font>");
		//设置高亮字段
		highlightBuilder.field(AggregatedSearch.TITLE);
		highlightBuilder.field(AggregatedSearch.CONTENT);

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
			return BeanUtil.copyToList(searchList, AggregatedSearchVO.class);
		}
		log.info("搜索未命中 searchText = {} type = {}", searchText, type);
		return new ArrayList<>();
	}

	@Override
	public PageResult aggregatedSearchEsPageList(SearchQueryEsRequest request) {
		return null;
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
		suggestBuilder.addSuggestion("suggestTitle", new CompletionSuggestionBuilder("titleSuggest").text(keyword));
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
	public void fetchArticles(String searchText, long current) {
		String url = String.format("https://kaifa.baidu.com/rest/v1/search?wd=elasticsearch&paramList=page_num=%s,page_size=10&pageNum=%s&pageSize=10", current, current);
		if (!StringUtil.isBlank(searchText) || !Objects.equals(searchText, "null")) {
			url = String.format("https://kaifa.baidu.com/rest/v1/search?wd=%s&paramList=page_num=%s,page_size=10&pageNum=%s&pageSize=10", searchText, current, current);
		}
		String res = HttpRequest.get(url).execute().body();
		String sourceUrl = URLUtil.url(url).getHost();
		//2、处理数据
		if (StringUtils.isBlank(res)) {
			log.error("code-nav.cn /api/post/search/page/vo res is null res = {}", res);
			return;
		}
		Map<String, Object> dataMap = JSONUtil.toBean(res, Map.class);
		String urlStatus = (String) dataMap.get("status");
		if (StringUtil.isBlank(urlStatus)) {
			log.error("code-nav.cn /api/post/search/page/vo 接口调用失败 code = {}", urlStatus);
			return;
		}
		JSONObject data = (JSONObject) dataMap.get("data");
		if (ObjectUtils.isEmpty(data)) {
			log.error("code-nav.cn /api/post/search/page/vo data is null data = {}", data);
			return;
		}
		JSONObject documents = (JSONObject) data.get("documents");
		if (ObjectUtils.isEmpty(documents)) {
			log.error("code-nav.cn /api/post/search/page/vo documents is null records = {}", documents);
			return;
		}

		JSONArray records = (JSONArray) documents.get("data");
		if (ObjectUtils.isEmpty(records)) {
			log.error("code-nav.cn /api/post/search/page/vo records is null records = {}", records);
			return;
		}

		List<AggregatedSearch> searches = new ArrayList<>(records.size());
		List<Article> articles = new ArrayList<>(records.size());
		//处理文章数据
		for (Object itemData : records) {
			JSONObject item = (JSONObject) itemData;
			JSONObject newItem = (JSONObject) item.get("techDocDigest");
			if (ObjectUtil.isEmpty(newItem)) continue;
			AggregatedSearch search = new AggregatedSearch();
			Article article = new Article();


			long snowflakeNextId = IdUtil.getSnowflakeNextId();

			//文章标题
			String title = newItem.getStr("title");
			if (StringUtil.isBlank(title) || title.equals("null")) continue;

			//内容
			String content = newItem.getStr("summary");
			if (StringUtil.isBlank(content) || content.equals("null")) continue;

			// 来源
			String articleUrl = newItem.getStr("url");
			if (StringUtil.isBlank(articleUrl) || articleUrl.equals("null")) continue;

			// 发布时间
			String publishTime = newItem.getStr("publishTime");
			if (StringUtil.isBlank(publishTime) || publishTime.equals("null")) continue;

			article.setId(snowflakeNextId);
			article.setTitle(title);
			article.setContent(content);
			article.setUrl(articleUrl);
			article.setSourceUrl(sourceUrl);
			article.setPublishTime(DateUtil.parse(publishTime, "yyyy-MM-dd HH:mm:ss"));


			search.setId(snowflakeNextId);
			search.setTitle(title);
			search.setContent(content);
			search.setType(SearchTypeEnum.Article.getType());


			searches.add(search);
			articles.add(article);
		}
		//3、入库,使用编程式事务
		TransactionStatus status = transactionManager.getTransaction(transactionDefinition);
		try {
			// 开始数据库操作
			baseMapper.saveAggregatedSearchList(searches);
			articleService.insertBatchArticles(articles);
			// 提交事务
			transactionManager.commit(status);
		} catch (RuntimeException e) {
			// 如果发生异常，则回滚事务
			transactionManager.rollback(status);
			log.error("出现异常为:{}", e);
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统异常");
		}
	}

	/**
	 * 爬取图片数据
	 *
	 * @param searchText
	 * @param first
	 */
	@Override
	public void fetchPictures(String searchText, long first) {
		String url = String.format("https://cn.bing.com/images/async?q=世界旅游胜地first=%s&mmasync=1", first);
		if (!StringUtil.isBlank(searchText) || !Objects.equals(searchText, "null")) {
			url = String.format("https://cn.bing.com/images/async?q=%s&first=%s&mmasync=1", searchText, first);
		}
		String sourceUrl = URLUtil.url(url).getHost();
		Document bingDoc = null;
		try {
			bingDoc = Jsoup.connect(url).get();
		} catch (IOException e) {
			log.error("big-picture api error = {}", e.getMessage());
			throw new RuntimeException(e);
		}
		//获取 图片列表 iuscp isv 元素
		Elements elements = bingDoc.select(".iuscp.isv");
		if (CollUtil.isEmpty(elements)) {
			log.error("bing-picture-html no element class .iuscp.isv");
			return;
		}
		List<AggregatedSearch> searches = new ArrayList<>(elements.size());
		List<Picture> pictures = new ArrayList<>(elements.size());
		for (Element element : elements) {
			AggregatedSearch search = new AggregatedSearch();
			Picture picture = new Picture();
			//获取 a标签
			Elements pic = element.select(".iusc");
			if (CollUtil.isEmpty(pic)) {
				log.error("bing-picture-html no element class .iusc ");
				continue;
			}
			//m 属性 图片数据：图片url、图片标题
			String m = pic.attr("m");
			if (StringUtils.isBlank(m)) {
				log.error("bing-picture-html no element property m");
				continue;
			}
			Map<String, Object> map = JSONUtil.toBean(m, Map.class);
			//获取图片url
			String picUrl = (String) map.get("turl");
			if (StringUtil.isBlank(picUrl) || picUrl.equals("null")) continue;
			Elements inflnk = element.select(".inflnk");
			//获取标题
			String picTitle = inflnk.attr("aria-label");
			if (StringUtil.isBlank(picTitle) || picTitle.equals("null")) continue;
			long snowflakeNextId = IdUtil.getSnowflakeNextId();
			picture.setUrl(picUrl);
			picture.setTitle(picTitle);
			picture.setSourceUrl(sourceUrl);
			picture.setId(snowflakeNextId);
			search.setType(SearchTypeEnum.PICTURE.getType());
			search.setId(snowflakeNextId);
			search.setTitle(picTitle);
			log.info("bing-picture-data url = {} title = {} source = {}", picUrl, picTitle, sourceUrl);
			pictures.add(picture);
			searches.add(search);
		}

		//3、入库,使用编程式事务
		TransactionStatus status = transactionManager.getTransaction(transactionDefinition);
		try {
			// 开始数据库操作
			baseMapper.saveAggregatedSearchList(searches);
			pictureService.insertBatchPictures(pictures);
			// 提交事务
			transactionManager.commit(status);
		} catch (RuntimeException e) {
			// 如果发生异常，则回滚事务
			transactionManager.rollback(status);
			log.error("出现异常为:{}", e);
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统异常");
		}
	}

	/**
	 * 爬取视频数据
	 *
	 * @param searchText
	 * @param first
	 */
	@Override
	public void fetchVideos(String searchText, long first) {
		String url = String.format("https://kaifa.baidu.com/rest/v1/search?wd=elasticsearch&paramList=page_num=%s,page_size=12,resource_type=VIDEO&pageNum=%s&pageSize=10", first, first);
		if (!StringUtil.isBlank(searchText) || !Objects.equals(searchText, "null")) {
			url = String.format("https://kaifa.baidu.com/rest/v1/search?wd=%s&paramList=page_num=%s,page_size=12,resource_type=VIDEO&pageNum=%s&pageSize=10", searchText, first, first);
		}
		String res = HttpRequest.get(url).execute().body();
		String sourceUrl = URLUtil.url(url).getHost();
		//2、处理数据
		if (StringUtils.isBlank(res)) {
			log.error("code-nav.cn /api/post/search/page/vo res is null res = {}", res);
			return;
		}
		Map<String, Object> dataMap = JSONUtil.toBean(res, Map.class);
		String urlStatus = (String) dataMap.get("status");
		if (StringUtil.isBlank(urlStatus)) {
			log.error("code-nav.cn /api/post/search/page/vo 接口调用失败 code = {}", urlStatus);
			return;
		}
		JSONObject data = (JSONObject) dataMap.get("data");
		if (ObjectUtils.isEmpty(data)) {
			log.error("code-nav.cn /api/post/search/page/vo data is null data = {}", data);
			return;
		}
		JSONObject documents = (JSONObject) data.get("documents");
		if (ObjectUtils.isEmpty(documents)) {
			log.error("code-nav.cn /api/post/search/page/vo documents is null records = {}", documents);
			return;
		}

		JSONArray records = (JSONArray) documents.get("data");
		if (ObjectUtils.isEmpty(records)) {
			log.error("code-nav.cn /api/post/search/page/vo records is null records = {}", records);
			return;
		}

		List<AggregatedSearch> searches = new ArrayList<>(records.size());
		List<Video> videos = new ArrayList<>(records.size());
		//处理视频数据
		for (Object itemData : records) {
			JSONObject item = (JSONObject) itemData;
			JSONObject newItem = (JSONObject) item.get("techDocDigest");
			if (ObjectUtil.isEmpty(newItem)) continue;
			AggregatedSearch search = new AggregatedSearch();
			Video video = new Video();

			long snowflakeNextId = IdUtil.getSnowflakeNextId();
			search.setId(snowflakeNextId);
			video.setId(snowflakeNextId);

			//视频标题
			String title = newItem.getStr("title");
			if (StringUtil.isBlank(title) || title.equals("null")) continue;
			search.setTitle(title);
			video.setTitle(title);
			//封面
			String keywords = (String) newItem.get("subKeywords");
			if (ObjectUtil.isEmpty(keywords)) continue;
			List<String> keyList = JSONUtil.toList(keywords, String.class);
			Map<String, Object> keyMap = JSONUtil.toBean(keyList.get(0), Map.class);
			String cover = (String) keyMap.get("value");
			String videoUrl = newItem.getStr("url");
			video.setUrl(videoUrl);
			video.setCover(cover);
			video.setSourceUrl(sourceUrl);

			search.setType(SearchTypeEnum.VIDEO.getType());
			searches.add(search);
			videos.add(video);
		}
		//3、入库,使用编程式事务
		TransactionStatus status = transactionManager.getTransaction(transactionDefinition);
		try {
			// 开始数据库操作
			baseMapper.saveAggregatedSearchList(searches);
			videoService.insertBatchVideos(videos);
			// 提交事务
			transactionManager.commit(status);
		} catch (RuntimeException e) {
			// 如果发生异常，则回滚事务
			transactionManager.rollback(status);
			log.error("出现异常为:{}", e);
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统异常");
		}
	}

	@Override
	public void saveSearchTextAndCrawlerData(QueryRequest searchQueryRequest) {

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
}
