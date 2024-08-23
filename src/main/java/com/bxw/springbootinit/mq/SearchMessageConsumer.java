package com.bxw.springbootinit.mq;

import com.bxw.springbootinit.adapter.datasource.DataSource;
import com.bxw.springbootinit.adapter.service.ServiceAdapter;
import com.bxw.springbootinit.manager.SearchFacade;
import com.bxw.springbootinit.model.dto.mq.InsertBatchDTO;
import com.bxw.springbootinit.model.entity.AggregatedSearch;
import com.bxw.springbootinit.model.enums.OperationTypeEnum;
import com.bxw.springbootinit.registry.datasource.DataSourceRegistry;
import com.bxw.springbootinit.registry.service.TypeServiceRegistry;
import com.rabbitmq.client.Channel;
import com.bxw.springbootinit.constant.SearchMqConstant;
import com.bxw.springbootinit.model.dto.query.QueryRequest;
import com.bxw.springbootinit.service.AggregatedSearchService;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * bi 消息消费者
 *
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

    //指定程序监听的消息队列和确认机制
    @RabbitListener(queues = { SearchMqConstant.SEARCH_QUEUE_NAME }, ackMode = "MANUAL")
    public void biReceiveMessage(InsertBatchDTO message, Channel channel, @Header(value = AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("消息消费中...,具体的操作为:{}", Objects.requireNonNull(OperationTypeEnum.getEnumByType(message.getFlag())).getValue());
        log.info("biReceiveMessage message = {} deliveryTag = {}", message, deliveryTag);
        try {
            //保存搜索记录 和 爬取数据入库
//            searchService.saveSearchTextAndCrawlerData(message);
            int flag = message.getFlag();
            List<?> dataList = message.getDataList();
            if(OperationTypeEnum.REDIS.getType().equals(flag)){
                String key = message.getKey();
                searchFacade.saveSearchList(key,dataList);
            }else if(OperationTypeEnum.MYSQL.getType().equals(flag)){
                List<AggregatedSearch> searches = message.getSearches();
                String type = message.getType();
                searchService.saveSearchTextAndCrawlerData(searches,dataList,type);
            }
            //确认消息
            channel.basicAck(deliveryTag, false);
            log.info("消息消费成功,具体的操作为:{}", Objects.requireNonNull(OperationTypeEnum.getEnumByType(message.getFlag())).getValue());
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
