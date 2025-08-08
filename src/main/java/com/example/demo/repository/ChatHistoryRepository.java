package com.example.demo.repository;

import java.util.List;

public interface ChatHistoryRepository {

    /**
     * 保存会话记录ID
     * @param type 业务类型 chat,server...
     * @param chatId 会话ID
     */
    void saveChatId(String type, String chatId);

    /**
     * 查询会话记录ID
     * @param type 业务类型 chat,server...
     * @return 会话ID列表
     */
    List<String> queryChatIds(String type);
}
