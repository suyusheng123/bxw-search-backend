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
import com.bxw.springbootinit.utils.InstallCertUtils;
import com.bxw.springbootinit.utils.TrustAllCertManagerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
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
        ThrowUtils.throwIf(size > 100, ErrorCode.PARAMS_ERROR);
        String searchText = pictureQueryRequest.getSearchText();

        Page<Picture> picturePage = pictureService.searchPicture(searchText, current, size);
        return ResultUtils.success(picturePage);
    }


//    @GetMapping("/img/{proxy}")
//    public void getImg(@PathVariable String proxy, HttpServletResponse response) throws Exception {
//        String imageUrl = new String(Base64.getDecoder().decode(proxy), StandardCharsets.UTF_8);
//        TrustAllCertManagerUtils.rawDataHomePage(imageUrl);
//        ResponseEntity<byte[]> responseEntity = new RestTemplate().exchange(imageUrl, HttpMethod.GET,null, byte[].class);
//
//
//        //获取entity中的数据
//        byte[] body = responseEntity.getBody();
//        //创建输出流  输出到本地
//        OutputStream os = response.getOutputStream();
//
//        // 将图片数据流写入响应输出流
//        if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
//            response.setContentType(MediaType.IMAGE_JPEG_VALUE); // 设置响应内容类型
//            response.getOutputStream().write(responseEntity.getBody()); // 将图片数据写入响应输出流
//        } else {
//            response.setStatus(HttpStatus.NOT_FOUND.value()); // 处理请求失败的情况
//        }
//    }
    // endregion
}
