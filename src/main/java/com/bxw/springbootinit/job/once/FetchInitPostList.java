package com.bxw.springbootinit.job.once;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bxw.springbootinit.esdao.PostEsDao;
import com.bxw.springbootinit.model.dto.post.PostEsDTO;
import com.bxw.springbootinit.model.entity.Post;
import com.bxw.springbootinit.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 获取初始化的同步帖子
 *
 */
// todo 取消注释开启任务
//@Component
@Slf4j
public class FetchInitPostList implements CommandLineRunner {

    @Resource
    private PostService postService;


    @Override
    public void run(String... args) {
        String json = "{\"pageSize\":12,\"sortOrder\":\"descend\",\"sortField\":\"createTime\",\"tags\":[\"笔记\"],\"current\":1,\"reviewStatus\":1,\"category\":\"文章\",\"hiddenContent\":true}";
        String url = "http://api.code-nav.cn/api/post/list/page/vo";
        String result = HttpRequest.post(url)
                .body(json)
                .execute().body();

        // json转对象
        Map<String, Object> map = JSONUtil.toBean(result, Map.class);
        // 获取抓取网站的数据
        JSONObject data = (JSONObject) map.get("data");
        if (data == null) return;
        JSONArray records = (JSONArray) data.get("records");
        if(records == null) return;
        // 将获取到的数据进行转化,转化成自己的Post对象
        List<Post> postList = new ArrayList<>();
        for (Object record : records) {
            if(record != null){
                JSONObject temp = (JSONObject) record;
                Post post = new Post();
                post.setTitle(temp.getStr("title"));
                post.setContent(temp.getStr("content"));
                // tags从数组转化成字符串类型
                JSONArray tags = (JSONArray) temp.get("tags");
                List<String> list = tags.toList(String.class);
                post.setTags(JSONUtil.toJsonStr(list));
                post.setThumbNum(temp.getInt("thumbNum"));
                post.setFavourNum(temp.getInt("favourNum"));
                post.setUserId(1L);
                postList.add(post);
            }
        }
        System.out.println(postList);
        boolean b = postService.saveBatch(postList);
        if (b){
            log.info("FetchInitPostList的条数为:{}",postList.size());
        }else{
            log.error("获取帖子失败");
        }
    }
}
