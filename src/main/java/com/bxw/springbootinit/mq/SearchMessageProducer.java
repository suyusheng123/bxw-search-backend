package com.bxw.springbootinit.mq;

import com.bxw.springbootinit.constant.SearchMqConstant;
import com.bxw.springbootinit.model.dto.mq.InsertBatchDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * bi 消息生产者
 */
@Component
@Slf4j
public class SearchMessageProducer {

	@Resource
	private RabbitTemplate rabbitTemplate;

	/**
	 * 发送消息
	 *
	 * @param message 消息
	 */
	public void sendMessage(InsertBatchDTO message) {
		log.info("消息发送到生产者中...key为{}", message.getMessageKey());
//        rabbitTemplate.convertAndSend(BiMqConstant.BI_CHART_EXCHANGE_NAME, BiMqConstant.BI_CHART_ROUTING_KEY, message);
		rabbitTemplate.convertAndSend(SearchMqConstant.SEARCH_EXCHANGE_NAME, SearchMqConstant.SEARCH_ROUTING_KEY, message,
				// 设置消息过期时间： 单位：毫秒
				message1 -> {
					message1.getMessageProperties().setExpiration(SearchMqConstant.SEARCH_MESSAGE_EXPIRED);// 消息过期时间
					message1.getMessageProperties().setDeliveryMode(MessageDeliveryMode.fromInt(2)); // 持久化
					// 返回消息对象
					return message1;
				});
		log.info("消息发送到生产者成功,key为{}", message.getMessageKey());
	}
}
