package com.example.yuaiagent.App;

import com.example.yuaiagent.advisor.MyLoggerAdvisor;
import com.example.yuaiagent.advisor.ReReadingAdvisor;
import com.example.yuaiagent.chatmemory.FileBasedChatMemory;
import com.example.yuaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.example.yuaiagent.rag.QueryRewriter;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;

import java.util.List;

@Component
@Slf4j
public class LoveApp {
    private final ChatClient chatClient;

//系统预设
    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";
    private static final String RECOMMEND_SYSTEM_PROMPT = """
        你是一名专业的恋爱对象匹配顾问。

        请根据用户的问题，从恋爱对象知识库中推荐可能适合的对象。

        要求：
        1. 只能推荐知识库中检索到的对象。
        2. 不要编造对象库中不存在的信息。
        3. 可以推荐 1 到 3 名对象。
        4. 说明每名对象与用户的匹配理由。
        5. 说明可能存在的不匹配点或注意事项。
        6. 如果用户提供的信息太少，请提醒用户补充性格、兴趣、生活方式和择偶要求。
        7. 使用普通自然语言回答，不要输出 JSON 或表格。
        8. 推荐结果仅供参考，不要描述成绝对结论。
        """;

    public LoveApp(ChatModel dashscopeChatModel) {
//        基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir, 10);

//        基于内存的对话记忆
//        ChatMemory chatMemory = MessageWindowChatMemory.builder()
//                .chatMemoryRepository(new InMemoryChatMemoryRepository())
//                .maxMessages(10)
//                .build();
//        多个请求共享的chatClient客户端
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
//                        自定义日志拦截器
                        new MyLoggerAdvisor()
//                        自定义推理增强拦截器，按需开启
//                        new ReReadingAdvisor()
                ) //默认预设
                .build();
    }

//    AI基础对话（支持多轮对话记忆）
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse= chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content =chatResponse.getResult().getOutput().getText();
        log.info("content:{}",content);
        return content;
    }

    /**
     * AI 流式对话。
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    record LoveReport(String title, List<String> suggestions){

    }

/*
* AI对话报告功能（演示结构化输出）
* */

    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport:{}",loveReport);
        return loveReport;
    }

    /*
    * AI恋爱知识库问答功能
    * */
    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private Advisor loveAppRagCloudAdvisor;

    @Resource
    private QueryRewriter queryRewriter;
    /*
    * 和rag进行对话
    * */
    public String doChatWithRag(String message, String chatId) {
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
//                使用改写后的查询rewrittenMessage，而非原始message
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))

//                开启日志
                .advisors(new MyLoggerAdvisor())

//              应用本地rag知识库问答，先注释掉，使用自己写的云知识库的数据
//                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())

//                云知识库的rag增强检索
//                .advisors(loveAppRagCloudAdvisor)

//               这里是基于pgvector的向量存储
//                .advisors(QuestionAnswerAdvisor.builder(pgVectorVectorStore).build())
//               使用自己创建的rag检索增强拦截器
                .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(loveAppVectorStore,"单身"))

                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    @Resource
    private VectorStore pgVectorVectorStore;
/*
* 推荐恋爱对象功能
* */
    public String doChatWithRagRecommend(String message, String chatId) {
        String recommendPrompt = """
            请根据恋爱对象知识库，分析下面这位用户适合哪些恋爱对象：

            用户问题：
            %s
            """.formatted(message.trim());

        ChatResponse chatResponse = chatClient
                .prompt()
                .system(RECOMMEND_SYSTEM_PROMPT)
                .user(recommendPrompt)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))

//                开启日志(构造函数开了这里就不开了)
//                .advisors(new MyLoggerAdvisor())
//              应用本地rag知识库问答，先注释掉，使用自己写的云知识库的数据
//                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())

//                这里是基于云知识库的rag增强检索先注释掉，使用pgvector的向量存储
//                .advisors(loveAppRagCloudAdvisor)

//               这里是基于pgvector的向量存储
                .advisors(QuestionAnswerAdvisor.builder(pgVectorVectorStore).build())
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("恋爱对象推荐结果: {}", content);
        return content;
    }

    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))

                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


//    把mcp所有的包都整合到toolCallbackProvider中
    @Autowired(required = false)
    private ToolCallbackProvider toolCallbackProvider;
    public String doChatWithMcp(String message, String chatId) {
        if (toolCallbackProvider == null) {
            throw new IllegalStateException(
                    "MCP 客户端未启用，请启动 localhost:8127 MCP 服务并设置 spring.ai.mcp.client.enabled=true");
        }
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))

                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}
