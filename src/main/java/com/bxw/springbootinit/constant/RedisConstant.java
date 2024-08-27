package com.bxw.springbootinit.constant;

/**
 * Redis-Key
 *
 */
public interface RedisConstant {

    /**
     * 搜索数据
     */
    String SEARCH_KEY = "search:";
    Long SEARCH_TTL = 1L;

    /**
     * 查询加锁
     */
    String SEARCH_LOCK_KEY = "lock:search:";

    /**
     * 发送消息加锁
     */
    String MESSAGE_LOCK_KEY = "lock:message:";

    /**
     * 搜索建议加锁
     */
    String SUGGEST_LOCK_KEY = "lock:suggest:";

    /**
     * 消息状态加锁key
     */
    String MESSAGE_KEY = "message:search:";


    /**
     * 搜索建议内容key
     */
    String SUGGEST_SEARCH_KEY = "suggest:search:";
}
