package com.bxw.springbootinit.adapter.method.aggregatedservice;

import com.bxw.springbootinit.model.vo.SearchVO;

/**
 * ClassName: MethodAdapter
 * Description:
 * 同一个类里的方法适配器
 * @Author 坤坤学🐸
 * @Create 2024/8/25 16:43
 * @Version 1.0
 */
public interface FetchDataMethodAdapter {

	/**
	 * 爬取数据
	 */
	SearchVO fetchData(String searchText, long first);
}
