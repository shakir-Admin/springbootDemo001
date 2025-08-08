package com.example.demo.controller;

import com.example.demo.DTO.ChatBody;
import com.example.demo.DTO.MessageVO;
import com.example.demo.repository.InMemoryChatHistoryRepository;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/history")
public class ChatHistoryController {

    private final InMemoryChatHistoryRepository inMemoryChatHistoryRepository;

    private final ChatMemory chatMemory;

    @RequestMapping("/getChatIds")
    public List<String> getChatIds(@RequestBody ChatBody chatBody){
        return inMemoryChatHistoryRepository.queryChatIds(chatBody.getType());
    }

    @RequestMapping("/getChatHistoryByChatId")
    public List<MessageVO> getChatHistoryByChatId(@RequestBody ChatBody chatBody){
        List<Message> messageList = chatMemory.get(chatBody.getChatId(),Integer.MAX_VALUE);
        if(messageList == null){
            return List.of();
        }
        return messageList.stream().map(MessageVO::new).toList();
    }
}
