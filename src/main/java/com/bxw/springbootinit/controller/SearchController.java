package com.bxw.springbootinit.controller;

import com.bxw.springbootinit.common.BaseResponse;
import com.bxw.springbootinit.common.ResultUtils;
import com.bxw.springbootinit.manager.SearchFacade;
import com.bxw.springbootinit.model.dto.query.QueryRequest;
import com.bxw.springbootinit.model.vo.SearchVO;
import com.bxw.springbootinit.model.vo.SuggestVO;
import com.bxw.springbootinit.service.AggregatedSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 聚合搜索接口
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

	@Resource
	private SearchFacade searchFacade;

	@Resource
	private AggregatedSearchService aggregatedSearchService;
	/**
	 * 聚合搜索接口(废弃版)
	 * @param queryRequest
	 * @return
	 */
//	@PostMapping("/all")
//	public BaseResponse<SearchVO> searchAll(@RequestBody QueryRequest queryRequest, HttpServletRequest request) {
//		return ResultUtils.success(searchFacade.searchAll(queryRequest, request));
//	}

	/**
	 * 搜索建议接口
	 * @param keyword 搜索的关键字
	 * @return
	 */
	@GetMapping("/suggest")
	public BaseResponse<List<String>> getSearchSuggestList(@RequestParam(name = "keyword") String keyword) {
		return ResultUtils.success(searchFacade.getSuggestSeachList(keyword));
	}

	@PostMapping("/async")
	public BaseResponse<SearchVO> searchEs(@RequestBody QueryRequest queryRequest) {
		SearchVO searchVO = new SearchVO();
		searchFacade.searchAll(queryRequest,searchVO);
		return ResultUtils.success(searchVO);
	}


	// endregion
}
