package com.example.demo.repository;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryChatHistoryRepository implements ChatHistoryRepository{

    //会话ID map
    private final Map<String, List<String>> chatHistory = new HashMap<>();

    @Override
    public void saveChatId(String type, String chatId) {
//        if(!chatHistory.containsKey(type)){
//            chatHistory.put(type, new ArrayList<>());
//        }
//        List<String> chatIds = chatHistory.get(type);
        List<String> chatIds = chatHistory.computeIfAbsent(type, k -> new ArrayList<>());
        if(chatIds.contains(chatId)){
            return;
        }
        chatIds.add(chatId);
    }

    @Override
    public List<String> queryChatIds(String type) {
//        List<String> chatIds = chatHistory.get(type);
//        return chatIds == null ? new ArrayList<>() : chatIds;
        return chatHistory.getOrDefault(type, List.of());
    }
}
