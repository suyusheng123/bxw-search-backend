package com.bxw.springbootinit.adapter.datasource;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.datasource.DataSource;
import com.bxw.springbootinit.model.dto.post.PostQueryRequest;
import com.bxw.springbootinit.model.dto.user.UserQueryRequest;
import com.bxw.springbootinit.model.entity.Post;
import com.bxw.springbootinit.model.vo.PostVO;
import com.bxw.springbootinit.model.vo.UserVO;
import com.bxw.springbootinit.service.PostService;
import com.bxw.springbootinit.service.UserService;
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

	@Override
	public Page<PostVO> doSearch(String searchText, long current, long pageSize) {
		PostQueryRequest postQueryRequest = new PostQueryRequest();
		postQueryRequest.setSearchText(searchText);
		postQueryRequest.setCurrent((int)current);
		postQueryRequest.setPageSize((int)pageSize);
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (servletRequestAttributes != null) {
			HttpServletRequest request = servletRequestAttributes.getRequest();
			Page<Post> postPage = postService.listMyPostVOByPage(postQueryRequest);
			return postService.getPostVOPage(postPage,request);
		}
		return null;
	}
}
