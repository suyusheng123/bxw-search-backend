package com.bxw.springbootinit.service.impl;

import com.bxw.springbootinit.service.RedisOperationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: RedisOperationImpl
 * Description:
 * Redis业务实现类
 * @Author 坤坤学🐸
 * @Create 2024/8/24 9:33
 * @Version 1.0
 */
@Service
@Slf4j
public class RedisOperationServiceImpl implements RedisOperationService {

	@Resource
	private StringRedisTemplate stringRedisTemplate;



	/**
	 * 设置消息的状态key,防止插入重复消息
	 * @param key
	 * @return
	 */
	@Override
	public void setMessageStatus(String key,String value) {
		log.info("设置消息状态开始,key为{},状态为{}",key,value);
		stringRedisTemplate.opsForValue().set(key,value);
		stringRedisTemplate.expire(key, 2, TimeUnit.MINUTES);
		log.info("设置消息状态成功为,key为{},状态为{},时间为2min",key,value);
	}

	/**
	 * 判断消息是否已经消费了
	 * @param key
	 * @return
	 */
	@Override
	public String getMessageStatus(String key) {
		log.info("正在获取消息的状态,key为{}",key);
		String messageStatus = stringRedisTemplate.opsForValue().get(key);
		log.info("获取消息的状态成功,key为{},状态为{}",key,messageStatus);
		// 如果消息状态为空的话,给他设置默认值0
		if(StringUtils.isBlank(messageStatus) || Objects.equals(messageStatus,"null")){
			log.info("消息状态为空，设置默认值为0，key为{},时间为2min",key);
			messageStatus = "0";
			stringRedisTemplate.opsForValue().set(key,messageStatus);
			stringRedisTemplate.expire(key, 2, TimeUnit.MINUTES);
			return stringRedisTemplate.opsForValue().get(key);
		}
		return messageStatus;
	}
}
