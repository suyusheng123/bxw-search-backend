package com.bxw.springbootinit.controller;

import co.elastic.clients.elasticsearch.nodes.Http;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.common.BaseResponse;
import com.bxw.springbootinit.common.ErrorCode;
import com.bxw.springbootinit.common.ResultUtils;
import com.bxw.springbootinit.exception.ThrowUtils;
import com.bxw.springbootinit.manager.SearchFacade;
import com.bxw.springbootinit.model.dto.picture.PictureQueryRequest;
import com.bxw.springbootinit.model.dto.post.PostQueryRequest;
import com.bxw.springbootinit.model.dto.query.QueryRequest;
import com.bxw.springbootinit.model.dto.user.UserQueryRequest;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.model.entity.Post;
import com.bxw.springbootinit.model.enums.SearchEnum;
import com.bxw.springbootinit.model.vo.PostVO;
import com.bxw.springbootinit.model.vo.SearchVO;
import com.bxw.springbootinit.model.vo.UserVO;
import com.bxw.springbootinit.service.PictureService;
import com.bxw.springbootinit.service.PostService;
import com.bxw.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;

/**
 * 聚合搜索接口
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

	@Resource
	private SearchFacade searchFacade;
	/**
	 * 分页获取列表(聚合接口)
	 * @param queryRequest
	 * @return
	 */
	@PostMapping("/all")
	public BaseResponse<SearchVO> searchAll(@RequestBody QueryRequest queryRequest, HttpServletRequest request) {
		return ResultUtils.success(searchFacade.searchAll(queryRequest, request));
	}
	// endregion
}
