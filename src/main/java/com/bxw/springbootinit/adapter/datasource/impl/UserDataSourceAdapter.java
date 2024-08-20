package com.bxw.springbootinit.adapter.datasource.impl;


import com.bxw.springbootinit.adapter.datasource.DataSource;
import com.bxw.springbootinit.model.dto.user.UserQueryRequest;
import com.bxw.springbootinit.model.vo.UserVO;
import com.bxw.springbootinit.service.AggregatedSearchService;
import com.bxw.springbootinit.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * ClassName:DataSourceAdapter
 * Package:com.bxw.springbootinit.adapter.datasource
 * Description:
 * 用户数据源适配器
 *
 * @Author 卜翔威
 * @Create 2024/8/14 10:37
 * @Version 1.0
 */

@Component
public class UserDataSourceAdapter implements DataSource<UserVO> {


	@Resource
	private AggregatedSearchService aggregatedSearchService;
	@Resource
	private UserService userService;

	@Override
	public void doSearch(String searchText, long current, long pageSize) {
		UserQueryRequest userQueryRequest = new UserQueryRequest();
		userQueryRequest.setUserName(searchText);
		userQueryRequest.setCurrent((int) current);
		userQueryRequest.setPageSize((int) pageSize);
		userService.listUserVOByPage(userQueryRequest);
	}

}
