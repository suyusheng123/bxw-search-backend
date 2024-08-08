package com.bxw.springbootinit.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.annotation.AuthCheck;
import com.bxw.springbootinit.common.BaseResponse;
import com.bxw.springbootinit.common.DeleteRequest;
import com.bxw.springbootinit.common.ErrorCode;
import com.bxw.springbootinit.common.ResultUtils;
import com.bxw.springbootinit.constant.UserConstant;
import com.bxw.springbootinit.exception.BusinessException;
import com.bxw.springbootinit.exception.ThrowUtils;
import com.bxw.springbootinit.model.dto.picture.PictureQueryRequest;
import com.bxw.springbootinit.model.dto.post.PostAddRequest;
import com.bxw.springbootinit.model.dto.post.PostEditRequest;
import com.bxw.springbootinit.model.dto.post.PostQueryRequest;
import com.bxw.springbootinit.model.dto.post.PostUpdateRequest;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.model.entity.Post;
import com.bxw.springbootinit.model.entity.User;
import com.bxw.springbootinit.model.vo.PostVO;
import com.bxw.springbootinit.service.PictureService;
import com.bxw.springbootinit.service.PostService;
import com.bxw.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 图片接口
 *
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
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        String searchText = pictureQueryRequest.getSearchText();

        Page<Picture> picturePage = pictureService.searchPicture(searchText, current, size);
        return ResultUtils.success(picturePage);
    }


    // endregion
}
