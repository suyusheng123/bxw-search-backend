package com.bxw.springbootinit.mq;

import com.bxw.springbootinit.constant.SearchMqConstant;
import com.bxw.springbootinit.manager.SearchFacade;
import com.bxw.springbootinit.model.dto.mq.InsertBatchDTO;
import com.bxw.springbootinit.model.entity.AggregatedSearch;
import com.bxw.springbootinit.registry.service.TypeServiceRegistry;
import com.bxw.springbootinit.service.AggregatedSearchService;
import com.bxw.springbootinit.service.RedisOperationService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * bi 消息消费者
 */
@Slf4j
@Component
public class SearchMessageConsumer {

	@Resource
	private TypeServiceRegistry typeServiceRegistry;

	@Resource
	private SearchFacade searchFacade;
	@Resource
	private AggregatedSearchService searchService;

	@Resource
	private RedisOperationService redisOperationService;

	//指定程序监听的消息队列和确认机制
	@RabbitListener(queues = {SearchMqConstant.SEARCH_QUEUE_NAME}, ackMode = "MANUAL")
	public void biReceiveMessage(InsertBatchDTO message, Channel channel, @Header(value = AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
		log.info("消息消费中...,key为:{}", message.getMessageKey());
		log.info("biReceiveMessage message = {} deliveryTag = {}", message, deliveryTag);
		try {
			//保存搜索记录 和 爬取数据入库
//            searchService.saveSearchTextAndCrawlerData(message);
			List<?> dataList = message.getDataList();
			List<AggregatedSearch> searches = message.getSearches();
			String type = message.getType();
			String messageKey = message.getMessageKey();
			// 判断这则消息是否被消费过了
			String messageStatus = redisOperationService.getMessageStatus(messageKey);
			if (Objects.equals(messageStatus, "2")) {
				log.info("消息已经被消费过了,key为:{}", messageKey);
				//确认消息
				channel.basicAck(deliveryTag, false);
				return;
			}
			//设置消息状态
			searchService.saveSearchTextAndCrawlerData(searches, dataList, type);
			//确认消息
			channel.basicAck(deliveryTag, false);
			redisOperationService.setMessageStatus(messageKey, "2");
			log.info("消息消费成功,key为:{}",messageKey);
		} catch (Exception e) {
			try {
				channel.basicNack(deliveryTag, false, false);
			} catch (IOException ex) {
				log.error("拒绝消息失败 error = {}", ex.getMessage());
			}
			log.error("任务处理失败 message = {} deliveryTag = {} error = {}", message, deliveryTag, e);
		}
	}
}
