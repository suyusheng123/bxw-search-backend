package com.bxw.springbootinit.mq;

import com.bxw.springbootinit.constant.SearchMqConstant;
import com.bxw.springbootinit.manager.SearchFacade;
import com.bxw.springbootinit.model.dto.mq.InsertBatchDTO;
import com.bxw.springbootinit.model.entity.AggregatedSearch;
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
 * bi 死信队列消息消费者
 */
@Slf4j
@Component
public class SearchMessageDlxConsumer {
	@Resource
	private SearchFacade searchFacade;

	@Resource
	private AggregatedSearchService searchService;


	@Resource
	private RedisOperationService redisOperationService;

	//指定程序监听的消息队列和确认机制
	@RabbitListener(queues = {SearchMqConstant.SEARCH_DLX_QUEUE_NAME}, ackMode = "MANUAL")
	public void biReceiveDlxMessage(InsertBatchDTO message, Channel channel, @Header(value = AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
		log.info("死信队列消息消费中...,key为:{}", message.getMessageKey());
		log.info("biReceiveDlxMessage message = {} deliveryTag = {}", message, deliveryTag);
		try {
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
				log.error("其他异常 error = {}", e.getMessage());
			}finally {
				log.error("消息消费失败,消息状态更改为消费失败,key为:{}",message.getMessageKey());
				redisOperationService.setMessageStatus(message.getMessageKey(), "0");
			}
			log.error("任务处理失败 message = {} deliveryTag = {} error = {}", message, deliveryTag, e.getMessage());
		}
	}
}
