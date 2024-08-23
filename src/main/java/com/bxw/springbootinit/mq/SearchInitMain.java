package com.bxw.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.bxw.springbootinit.constant.SearchMqConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * 创建bi程序用到的交换机和队列
 */
public class SearchInitMain {

    public static void main(String[] argv) throws Exception {
        //创建链接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        //建立链接、创建频道
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            //创建 esSearch 死信交换机
            channel.exchangeDeclare(SearchMqConstant.SEARCH_DLX_EXCHANGE_NAME, "direct");
            //创建Bi死信队列
            channel.queueDeclare(SearchMqConstant.SEARCH_DLX_QUEUE_NAME, true, false, false, null);
            channel.queueBind(SearchMqConstant.SEARCH_DLX_QUEUE_NAME, SearchMqConstant.SEARCH_DLX_EXCHANGE_NAME, SearchMqConstant.SEARCH_DLX_ROUTING_KEY);

            //创建 esSearch 交换机
            channel.exchangeDeclare(SearchMqConstant.SEARCH_EXCHANGE_NAME, "direct");
            //指定死信队列参数
            Map<String, Object> args = new HashMap<>(2);
            //要绑定那个死信交换机
            args.put("x-dead-letter-exchange", SearchMqConstant.SEARCH_DLX_EXCHANGE_NAME);
            //指定死信要发送到那个死信队列
            args.put("x-dead-letter-routing-key", SearchMqConstant.SEARCH_DLX_ROUTING_KEY);
            //创建 esSearch 队列 并 绑定 死信交换机
            channel.queueDeclare(SearchMqConstant.SEARCH_QUEUE_NAME, true, false, false, args);
            channel.queueBind(
                    SearchMqConstant.SEARCH_QUEUE_NAME,
                    SearchMqConstant.SEARCH_EXCHANGE_NAME,
                    SearchMqConstant.SEARCH_ROUTING_KEY
            );
        }
    }
}
