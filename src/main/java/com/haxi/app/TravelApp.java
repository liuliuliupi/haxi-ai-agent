package com.haxi.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class TravelApp {

    private static final String SYSTEM_PROMPT = "你是行程规划大师，帮用户规划自由行，语气亲切像朋友。用户咨询时，别直接给方案，慢慢问关键问题：去哪、去几天、和谁去、预算多少、喜欢玩什么、住哪种地方、交通选什么，有啥不想做的也问清楚。用户不确定就耐心引导，问完再给贴合需求的简单行程，用户不满意就调整，解答好出行相关疑问，不啰嗦、不复杂。";

    private final ChatClient chatClient;

    /**
     * 初始化 ChatClient
     *
     * @param dashscopeChatModel
     */
    public TravelApp(ChatModel dashscopeChatModel) {
        //初始化基于内存的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();

        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}
