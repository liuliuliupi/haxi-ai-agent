package com.haxi.app;

import com.haxi.advisor.MyLoggerAdvisor;
import com.haxi.advisor.ReReadingAdvisor;
import com.haxi.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Slf4j
public class TravelApp {

    private static final String SYSTEM_PROMPT =
            "角色：自由行规划大师 风格：热情像朋友，不说术语，重性价比，主动避坑。 核心策略：【先给草案，再细化】。信息不足时默认假设（如 3 天 2 晚），直接出初稿，禁止先查户口。" +
            "\n" +
            "规则：\n" +
            "交互：每次只问 1-2 个关键问题，别让用户累。\n" +
            "建议：推荐具体店名/路线，解释“为什么这么排”（如顺路/避坑）。\n" +
            "迭代：根据反馈调整方案，直到用户满意。\n" +
            "输出：最后生成简单行程表（时间 | 行程 | 建议 | 备注）。\n" +
            "工作流程：\n" +
            "首回：复述已知 + 假设缺失信息 + 给初稿框架 + 问 1-2 个偏好（如住宿/节奏）。\n" +
            "中间：确认修改点 + 更新方案 + 追问下一细节（如美食/交通）。\n" +
            "结尾：汇总所有确认信息，输出最终行程表 + 祝福。";

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
//                        ,new ReReadingAdvisor()
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


    /**
     * AI 基础对话（支持多轮对话记忆,SSE流式传输）
     *
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
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

    @Resource
    private Advisor travelAppRagCloudAdvisor;

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    /**
     * 和 RAG 知识库进行对话
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        // 查询重写
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                // 使用改写后的查询
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // 应用 RAG 知识库问答（基于内存）
                .advisors(QuestionAnswerAdvisor.builder(travelAppVectorStore).build())
                // 应用 RAG 检索增强服务（基于云知识库服务）
//                .advisors(travelAppRagCloudAdvisor)
                // 应用 RAG 检索增强服务（基于 PgVector 向量存储）
//                .advisors(QuestionAnswerAdvisor.builder(pgVectorVectorStore).build())
                // 应用自定义的 RAG 检索增强服务（文档查询器 + 上下文增强器）
//                .advisors(
//                        TravelAppRagCustomAdvisorFactory.createTravelAppRagCustomAdvisor(
//                                travelAppVectorStore, "自由"
//                        )
//                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    // AI 调用工具能力
    @Resource
    private ToolCallback[] allTools;

    /**
     * AI 行程规划报告功能（支持调用工具）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    // AI 调用 MCP 服务

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * AI 行程报告功能（调用 MCP 服务）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}
