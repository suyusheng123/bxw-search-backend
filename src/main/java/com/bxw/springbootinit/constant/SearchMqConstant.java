package com.bxw.springbootinit.constant;

/**
 * 搜索 mq 常量
 *
 * @author lwx
 * @since 2023/7/9 15:46
 */
public interface SearchMqConstant {

    /**
     * search 交换机
     */
    String SEARCH_EXCHANGE_NAME = "search-es-exchange";

    /**
     * search 路由键
     */
    String SEARCH_ROUTING_KEY = "search-es-routing";

    /**
     * search 队列
     */
    String SEARCH_QUEUE_NAME = "search-es-queue";

    /**
     * search 消息过期时间
     */
    String SEARCH_MESSAGE_EXPIRED = "20000";

    /**
     * search 死信交换机
     */
    String SEARCH_DLX_EXCHANGE_NAME = "search-es-dlx-exchange";

    /**
     * search 死信队列
     */
    String SEARCH_DLX_QUEUE_NAME = "search-es-dlx-queue";

    /**
     * search 死信路由键
     */
    String SEARCH_DLX_ROUTING_KEY = "search-es-dlx-routing";
}
