package com.bxw.springbootinit.adapter.datasource.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.adapter.datasource.DataSource;
import com.bxw.springbootinit.model.dto.post.PostQueryRequest;
import com.bxw.springbootinit.model.entity.AggregatedSearch;
import com.bxw.springbootinit.model.entity.Post;
import com.bxw.springbootinit.model.vo.PostVO;
import com.bxw.springbootinit.service.AggregatedSearchService;
import com.bxw.springbootinit.service.PostService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * ClassName:DataSourceAdapter
 * Package:com.bxw.springbootinit.adapter.datasource
 * Description:
 * 用户数据源适配器
 * @Author 卜翔威
 * @Create 2024/8/14 10:37
 * @Version 1.0
 */

@Component
public class PostDataSourceAdapter implements DataSource<PostVO> {

	@Resource
	private PostService postService;

	@Resource
	private AggregatedSearchService aggregatedSearchService;

//	@Override
//	public Page<PostVO> doSearch(String searchText, long current, long pageSize) {
//		PostQueryRequest postQueryRequest = new PostQueryRequest();
//		postQueryRequest.setSearchText(searchText);
//		postQueryRequest.setCurrent((int)current);
//		postQueryRequest.setPageSize((int)pageSize);
//		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//		if (servletRequestAttributes != null) {
//			HttpServletRequest request = servletRequestAttributes.getRequest();
////			Page<Post> postPage = postService.listMyPostVOByPage(postQueryRequest);
//			// 改用es的查询
//			Page<Post> postPage = postService.searchFromEs(postQueryRequest);
//			return postService.getPostVOPage(postPage,request);
//		}
//		return null;
//	}

	/**
	 * 爬取数据并入库
	 * @param searchText 搜索词
	 */
	@Override
	public void doSearch(String searchText,long current,long currentSize){
		aggregatedSearchService.fetchPostPassage(current,currentSize);
	}
}
