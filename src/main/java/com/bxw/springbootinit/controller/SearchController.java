package com.bxw.springbootinit.controller;

import co.elastic.clients.elasticsearch.nodes.Http;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.common.BaseResponse;
import com.bxw.springbootinit.common.ErrorCode;
import com.bxw.springbootinit.common.ResultUtils;
import com.bxw.springbootinit.exception.ThrowUtils;
import com.bxw.springbootinit.model.dto.picture.PictureQueryRequest;
import com.bxw.springbootinit.model.dto.post.PostQueryRequest;
import com.bxw.springbootinit.model.dto.query.QueryRequest;
import com.bxw.springbootinit.model.dto.user.UserQueryRequest;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.model.entity.Post;
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

/**
 * 聚合搜索接口
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

	@Resource
	private PictureService pictureService;

	@Resource
	private UserService userService;

	@Resource
	private PostService postService;

	/**
	 * 分页获取列表(聚合接口)
	 *
	 * @param queryRequest
	 * @return
	 */
	@PostMapping("/all")
	public BaseResponse<SearchVO> searchAll(@RequestBody QueryRequest queryRequest, HttpServletRequest request) {
		String searchText = queryRequest.getSearchText();
		if(searchText == null){
			searchText = "";
		}
		long current = queryRequest.getCurrent();
		long pageSize = queryRequest.getPageSize();

		Page<Picture> picturePage = pictureService.searchPicture(searchText, current, pageSize);

		UserQueryRequest userQueryRequest = new UserQueryRequest();
		userQueryRequest.setUserName(searchText);
		userQueryRequest.setCurrent((int) current);
		userQueryRequest.setPageSize((int) pageSize);
		Page<UserVO> userVOPage = userService.listUserVOByPage(userQueryRequest);

		PostQueryRequest postQueryRequest = new PostQueryRequest();
		postQueryRequest.setSearchText(searchText);
		postQueryRequest.setCurrent((int) current);
		postQueryRequest.setPageSize((int) pageSize);
		Page<Post> postPage = postService.listMyPostVOByPage(postQueryRequest);
		Page<PostVO> postVOPage = postService.getPostVOPage(postPage, request);

		SearchVO searchVO = new SearchVO();
		searchVO.setPictureList(picturePage.getRecords());
		searchVO.setUserList(userVOPage.getRecords());
		searchVO.setPostList(postVOPage.getRecords());

		return ResultUtils.success(searchVO);
	}


	// endregion
}
