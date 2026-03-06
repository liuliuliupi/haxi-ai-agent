package com.haxi.app;

import com.haxi.advisor.MyLoggerAdvisor;
import com.haxi.advisor.ReReadingAdvisor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

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
        // 初始化基于文件的对话记忆
//        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
//        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        //初始化基于内存的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();

        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor()
                        //自定义推理增强Advisor,可按需开启（用户消息*2，输入token翻倍，成本上升）
                        ,new ReReadingAdvisor()
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



    record TravelReport(String title, List<String> suggestions){

    }

    /**
     * AI旅行规划报告功能（实战结构化输出）
     *
     * @param message
     * @param chatId
     * @return
     */
    public TravelReport doChatWithReport(String message, String chatId) {
         TravelReport travelReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后生成行程规划列表，标题为{用户名}的行程报告，内容建议为列表")
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(TravelReport.class);
        log.info("travelReport: {}", travelReport);
        return travelReport;
    }


    // AI 行程知识库问答功能

    @Resource
    private VectorStore travelAppVectorStore;

//    @Resource
//    private Advisor travelAppRagCloudAdvisor;

//    @Resource
//    private VectorStore pgVectorVectorStore;

//    @Resource
//    private QueryRewriter queryRewriter;

    /**
     * 和 RAG 知识库进行对话
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        // 查询重写
//        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                // 使用改写后的查询
//                .user(rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用 RAG 知识库问答
                .advisors(QuestionAnswerAdvisor.builder(travelAppVectorStore).build())
                // 应用 RAG 检索增强服务（基于云知识库服务）
//                .advisors(travelAppRagCloudAdvisor)
                // 应用 RAG 检索增强服务（基于 PgVector 向量存储）
//                .advisors(QuestionAnswerAdvisor.builder(pgVectorVectorStore).build())
                // 应用自定义的 RAG 检索增强服务（文档查询器 + 上下文增强器）
//                .advisors(
//                        travelAppRagCustomAdvisorFactory.createtravelAppRagCustomAdvisor(
//                                travelAppVectorStore, "单身"
//                        )
//                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}
