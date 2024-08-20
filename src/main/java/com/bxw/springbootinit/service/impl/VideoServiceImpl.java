package com.bxw.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.common.ErrorCode;
import com.bxw.springbootinit.exception.BusinessException;
import com.bxw.springbootinit.model.entity.Video;
import com.bxw.springbootinit.model.enums.SearchTypeEnum;
import com.bxw.springbootinit.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 视频服务类实现
 *
 */
@Slf4j
@Service
public class VideoServiceImpl implements VideoService {

    @Override
    public Page<Video> searchVideoPageList(String searchText, long current, long pageSize) {
        if (StringUtils.isBlank(searchText)) {
            searchText = SearchTypeEnum.VIDEO.getText();
        }
        long pageCurrent = current < 0 ? 0 : current;
        long pageNum = pageSize < 0 ? 20 : pageSize;
        if (pageNum > 40) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //获取bing html
        String url = String.format("https://cn.bing.com/videos/search?&q=%s", searchText);
        Document bingDoc = null;
        try {
            bingDoc = Jsoup.connect(url).get();
        } catch (IOException e) {
            log.error("get bing-video-html error = ", e);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "数据获取异常");
        }
        log.info("bing-video-html = {}", bingDoc);
        //获取 视频列表 mc_vtvc_con_rc 元素
        Elements elements = bingDoc.select(".mc_vtvc_con_rc");
        if (CollUtil.isEmpty(elements)) {
            log.error("bing-video-html element .mc_vtvc_con_rc not null");
            return new Page<>();
        }
        List<Video> videoList = new ArrayList<>(elements.size());
        for (Element element : elements) {
            //视频封面
            String ourl = element.attr("vscm");
            Map<String, Object> ourlMap = JSONUtil.toBean(ourl, Map.class);
            String videoCover = (String) ourlMap.get("turl");

            //获取 div标签
            Elements pic = element.select(".vrhdata");
            if (CollUtil.isEmpty(pic)) {
                log.error("bing video-html .vrhdata element is null");
                continue;
            }

            //vrhm 视频数据
            String vrhm = pic.attr("vrhm");
            if (StringUtils.isBlank(vrhm)) {
                log.error("bing video-html vrhm property is null");
                continue;
            }
            Map<String, Object> map = JSONUtil.toBean(vrhm, Map.class);
            //视频url
            String videoUrl = (String) map.get("murl");
            //视频标题
            String videoTitle = (String) map.get("vt");

            //实体
            Video video = new Video();
            if (StringUtils.isNoneBlank(videoTitle, videoUrl, videoCover)) {
                video.setUrl(videoUrl);
                video.setTitle(videoTitle);
                video.setCover(videoCover);
            }
            log.info("bing-video url = {} title = {} cover = {}", videoUrl, videoTitle, videoCover);
            videoList.add(video);
            //分页
            if (videoList.size() >= pageNum) {
                break;
            }
        }
        if (CollUtil.isEmpty(videoList)) {
            return new Page<>();
        }
        long size = (pageCurrent - 1) * pageNum;
        Page<Video> page = new Page<>(size, pageNum);
        page.setRecords(videoList);
        return page;
    }
}
