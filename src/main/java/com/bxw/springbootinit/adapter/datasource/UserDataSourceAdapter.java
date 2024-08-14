package com.bxw.springbootinit.adapter.datasource;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.datasource.DataSource;
import com.bxw.springbootinit.model.dto.user.UserQueryRequest;
import com.bxw.springbootinit.model.vo.UserVO;
import com.bxw.springbootinit.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

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
	private UserService userService;

	@Override
	public Page<UserVO> doSearch(String searchText, long current, long pageSize) {
		UserQueryRequest userQueryRequest = new UserQueryRequest();
		userQueryRequest.setUserName(searchText);
		userQueryRequest.setCurrent((int) current);
		userQueryRequest.setPageSize((int) pageSize);
		return userService.listUserVOByPage(userQueryRequest);
	}
}
