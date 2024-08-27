package com.bxw.springbootinit.service;

import java.util.concurrent.TimeUnit;

/**
 * ClassName: RedisOperation
 * Description:
 * Redis操作业务类
 * @Author 坤坤学🐸
 * @Create 2024/8/24 9:31
 * @Version 1.0
 */
public interface RedisOperationService {

	/**
	 * 设置插入数据库的消息状态key，防止重复插入数据
	 */
	void setMessageStatus(String key,String value);

	/**
	 * 判断消息是否已经消费了
	 */
	String getMessageStatus(String key);
}
