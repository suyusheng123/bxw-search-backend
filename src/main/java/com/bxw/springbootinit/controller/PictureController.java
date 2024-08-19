package com.bxw.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.common.BaseResponse;
import com.bxw.springbootinit.common.ErrorCode;
import com.bxw.springbootinit.common.ResultUtils;
import com.bxw.springbootinit.exception.ThrowUtils;
import com.bxw.springbootinit.model.dto.picture.PictureQueryRequest;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 图片接口
 */
@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

	@Resource
	private PictureService pictureService;

	/**
	 * 分页获取列表（封装类）
	 *
	 * @param pictureQueryRequest
	 * @param request
	 * @return
	 */
	@PostMapping("/list/page/vo")
	public BaseResponse<Page<Picture>> listPostVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
	                                                    HttpServletRequest request) {
		long current = pictureQueryRequest.getCurrent();
		long size = pictureQueryRequest.getPageSize();
		// 限制爬虫
		ThrowUtils.throwIf(size > 100, ErrorCode.PARAMS_ERROR);
		String searchText = pictureQueryRequest.getSearchText();

		Page<Picture> picturePage = pictureService.searchPicture(searchText, current, size);
		return ResultUtils.success(picturePage);
	}

	// endregion
}
