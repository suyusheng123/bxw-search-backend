package com.bxw.springbootinit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
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
import com.bxw.springbootinit.model.entity.AggregatedSearch;
import com.bxw.springbootinit.model.entity.User;
import com.bxw.springbootinit.model.enums.SearchTypeEnum;
import com.bxw.springbootinit.model.vo.AggregatedSearchVO;
import com.bxw.springbootinit.model.vo.PageResult;
import com.bxw.springbootinit.model.vo.SuggestVO;
import com.bxw.springbootinit.model.vo.UserVO;
import com.bxw.springbootinit.service.AggregatedSearchService;
import com.bxw.springbootinit.service.UserService;
import com.bxw.springbootinit.utils.SqlUtils;
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

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
	private ElasticsearchRestTemplate elasticsearchRestTemplate;
	@Resource
	private UserService userService;

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
		boolQueryBuilder.filter(QueryBuilders.termQuery(AggregatedSearch.IS_DELETE, 0));
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
			List<AggregatedSearchVO> searchVoList = BeanUtil.copyToList(searchList, AggregatedSearchVO.class);
			//如果是 搜索帖子需要查询用户数据
			if (SearchTypeEnum.POST.getType().intValue() == type.intValue()) {
				List<Long> userIdList = searchHitList.stream().map(searchHit ->
								Long.parseLong(searchHit.getContent().getUserId())).
						collect(Collectors.toList());
				Map<Long, List<AggregatedSearchVO>> idSearchList = searchVoList.stream().filter(searchVO -> searchVO.getUserId() != null).collect(Collectors.groupingBy(AggregatedSearchVO::getUserId));
				List<User> userList = userService.listByIds(userIdList);
				if (CollUtil.isNotEmpty(userList)) {
					for (User user : userList) {
						Long userId = user.getId();
						if (idSearchList.containsKey(userId)) {
							List<AggregatedSearchVO> searchEsDTOList = idSearchList.get(userId);
							for (AggregatedSearchVO searchEsDTO : searchEsDTOList) {
								UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
								searchEsDTO.setUser(userVO);
							}
						} else {
							log.error("post userId error postData = {} userId = {}", idSearchList.get(userId), userId);
						}
					}
				}
			}
			return searchVoList;
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
	 * 爬取帖子的数据
	 *
	 * @param current 页码
	 */
	@Override
	public void fetchPostPassage(long current, long currentSize) {
		//1、获取数据
		String url = "http://api.code-nav.cn/api/news/list/page/vo";
		String json = String.format("{\n" +
				"  \"current\": %s,\n" +
				"  \"pageSize\": %s,\n" +
				"  \"sortField\": \"publishTime\",\n" +
				"  \"sortOrder\": \"descend\",\n" +
				"  \"searchText\": \"elas\"\n" +
				"}", current, currentSize);
		String res = HttpRequest.post(url).body(json).execute().body();
		String sourceUrl = URLUtil.url(url).getHost();
		log.info("code-nav.cn /api/post/search/page/vo jsonStr = {}", res);
		//2、处理数据
		if (StringUtils.isBlank(res)) {
			log.error("code-nav.cn /api/post/search/page/vo res is null res = {}", res);
			return;
		}
		Map<String, Object> dataMap = JSONUtil.toBean(res, Map.class);
		int code = (int) dataMap.get("code");
		if (code != 0) {
			log.error("code-nav.cn /api/post/search/page/vo 接口调用失败 code = {}", code);
			return;
		}
		JSONObject data = (JSONObject) dataMap.get("data");
		if (ObjectUtils.isEmpty(data)) {
			log.error("code-nav.cn /api/post/search/page/vo data is null data = {}", data);
			return;
		}
		JSONArray records = (JSONArray) data.get("records");
		if (ObjectUtils.isEmpty(records)) {
			log.error("code-nav.cn /api/post/search/page/vo records is null records = {}", records);
			return;
		}
		List<AggregatedSearch> postList = new ArrayList<>(records.size());
		//处理帖子数据
		for (Object itemData : records) {
			JSONObject item = (JSONObject) itemData;
			AggregatedSearch post = new AggregatedSearch();
			//帖子id
			String idStr = item.getStr("id");
			post.setId(Long.valueOf(idStr));
			//帖子标题
			String title = item.getStr("title");
			if (StringUtils.isNotBlank(title)) {
				post.setTitle(title);
			}
			//文章
			String content = item.getStr("description");
			if (StringUtils.isNotBlank(content)) {
				post.setContent(doStr(content));
			}
			//标签
			JSONArray tags = (JSONArray) item.get("tags");
			if (ObjectUtils.isNotEmpty(tags)) {
				List<String> tagList = tags.toList(String.class);
				String tagsStr = JSONUtil.toJsonStr(tagList);
				post.setTags(tagsStr);
			}
			//创建人id
//			post.setUserId(randomUserId());
			post.setType(SearchTypeEnum.POST.getType());
			post.setSourceUrl(sourceUrl);
			String linkUrl = item.getStr("linkUrl");
			post.setUrl(linkUrl);
			postList.add(post);
		}
		//3、过滤 标题、内容、标签 中包含 自我介绍的帖子
		final String[] filterName = {"自我介绍", "实习", "秋招", "新人", "实习生"};
		List<AggregatedSearch> aggregatedSearchList = postList.stream().filter(post -> !StringUtils.containsAny(post.getTitle(), filterName)).collect(Collectors.toList());
		//4、入库
		if (!this.baseMapper.saveAggregatedSearchList(aggregatedSearchList)) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "插入帖子数据失败");
		}
	}

	/**
	 * 爬取图片数据
	 *
	 * @param searchText
	 * @param first
	 */
	@Override
	public void fetchPicturePassage(String searchText, long first) {
		String url = String.format("https://cn.bing.com/images/async?q=世界旅游胜地first=%s&mmasync=1", first);
		if (searchText != null && !searchText.trim().isEmpty()) {
			url = String.format("https://cn.bing.com/images/async?q=%s&first=%s&mmasync=1", searchText, first);
		}
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
		List<AggregatedSearch> pictureList = new ArrayList<>(elements.size());
		for (Element element : elements) {
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
			Elements inflnk = element.select(".inflnk");
			//获取标题
			String picTitle = inflnk.attr("aria-label");
			//获取来源
			String sourceUrl = (String) map.get("purl");
			AggregatedSearch picture = new AggregatedSearch();
			picture.setUrl(picUrl);
			picture.setTitle(picTitle);
			picture.setSourceUrl(sourceUrl);
			picture.setType(SearchTypeEnum.PICTURE.getType());
			log.info("bing-picture-data url = {} title = {} source = {}", picUrl, picTitle, sourceUrl);
			pictureList.add(picture);
		}
		//过滤 图片 标题、url 为空的数据
		List<AggregatedSearch> filterPictureList = pictureList.stream().filter(picture -> StringUtils.isNotBlank(picture.getTitle()) || StringUtils.isNotBlank(picture.getUrl())).collect(Collectors.toList());
		//入库
		if (!this.baseMapper.saveAggregatedSearchList(filterPictureList)) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "插入图片数据失败");
		}
	}

	/**
	 * 爬取视频数据
	 *
	 * @param searchText
	 * @param first
	 */
	@Override
	public void fetchVideoPassage(String searchText, long first) {
		String url = String.format("https://cn.bing.com/videos/async/rankedans?q=%s&mmasync=1&first=%s&varh=VideoResultInfiniteScroll&vdpp=VideoResultAsync",
				"视频", first);
		if (searchText != null && !searchText.trim().isEmpty()) {
			url = String.format("https://cn.bing.com/videos/async/rankedans?q=%s&mmasync=1&first=%s&varh=VideoResultInfiniteScroll&vdpp=VideoResultAsync",
					searchText, first);
		}
		Document bingDoc = null;
		try {
			bingDoc = Jsoup.connect(url).get();
		} catch (IOException e) {
			log.error("bing-video api error ={}", e.getMessage());
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "数据获取异常");
		}
		Elements elements = bingDoc.select(".mm_vdcv_cnt .mc_fgvc");
		if (CollUtil.isEmpty(elements)) {
			log.error("bing-video-html no element class .mc_vtvc_con_rc");
			return;
		}
		List<AggregatedSearch> videoList = new ArrayList<>(elements.size());
		for (Element element : elements) {
			//视频封面
			String ourl = element.attr("vscm");
			Map<String, Object> ourlMap = JSONUtil.toBean(ourl, Map.class);
			String videoCover = (String) ourlMap.get("turl");
			//获取 .vrhdata 元素（视频数据）
			Elements pic = element.select(".vrhdata");
			if (CollUtil.isEmpty(pic)) {
				log.error("bing-video-html no element class .vrhdata");
				continue;
			}

			// vrhm 视频数据
			String vrhm = pic.attr("vrhm");
			if (StringUtils.isBlank(vrhm)) {
				log.error("bing-video-html vrhm property is null");
				continue;
			}
			Map<String, Object> map = JSONUtil.toBean(vrhm, Map.class);
			//视频url
			String videoUrl = (String) map.get("murl");
			//视频标题
			String videoTitle = (String) map.get("vt");

			AggregatedSearch video = new AggregatedSearch();
			video.setUrl(videoUrl);
			video.setTitle(videoTitle);
			video.setCover(videoCover);
			video.setType(SearchTypeEnum.VIDEO.getType());
			log.info("bing-video-data url = {} title = {} cover = {}", videoUrl, videoTitle, videoCover);
			videoList.add(video);
		}
		//过滤 视频 标题、url 为空的数据
		List<AggregatedSearch> filterVideoList = videoList.stream().filter(picture -> StringUtils.isNotBlank(picture.getTitle()) || StringUtils.isNotBlank(picture.getUrl())).collect(Collectors.toList());
		//入库
		if (!this.baseMapper.saveAggregatedSearchList(filterVideoList)) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "插入视频数据失败");
		}
	}

	@Override
	public void saveSearchTextAndCrawlerData(QueryRequest searchQueryRequest) {

	}

	/**
	 * 随机id
	 */
	private long randomUserId() {
		List<Long> userIdList = Arrays.asList(1660114534028922883L, 1660114534028922884L, 1660114534028922885L, 1660114534028922886L, 1660114534028922887L, 1660114534028922888L, 1660114534028922889L, 1660114534028922890L, 1660114534028922891L, 1660114534028922892L);
		return RandomUtil.randomEleList(userIdList, 1).get(0);
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
