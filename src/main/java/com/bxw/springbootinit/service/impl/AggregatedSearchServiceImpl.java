package com.bxw.springbootinit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.sql.QueryRequest;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bxw.springbootinit.common.ErrorCode;
import com.bxw.springbootinit.constant.CommonConstant;
import com.bxw.springbootinit.exception.BusinessException;
import com.bxw.springbootinit.mapper.AggregatedSearchMapper;
import com.bxw.springbootinit.model.dto.query.AggregatedSearchEsDTO;
import com.bxw.springbootinit.model.dto.query.SearchQueryEsRequest;
import com.bxw.springbootinit.model.entity.AggregatedSearch;
import com.bxw.springbootinit.model.vo.AggregatedSearchVO;
import com.bxw.springbootinit.model.vo.PageResult;
import com.bxw.springbootinit.model.vo.SuggestVO;
import com.bxw.springbootinit.service.AggregatedSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.suggest.response.CompletionSuggestion;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
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

	@Override
	public List<AggregatedSearchVO> aggregatedSearchEs(SearchQueryEsRequest request) {
		return null;
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

	@Override
	public void fetchPostPassage(int current) {

	}

	@Override
	public void fetchPicturePassage(String searchText) {

	}

	@Override
	public void fetchVideoPassage(String searchText) {

	}

	@Override
	public void saveSearchTextAndCrawlerData(QueryRequest searchQueryRequest) {

	}
}
