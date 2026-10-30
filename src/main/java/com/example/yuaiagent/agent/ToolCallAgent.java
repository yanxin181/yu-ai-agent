package com.example.yuaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.example.yuaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/*
* 具体实现think和act方法
*
*
* */
@EqualsAndHashCode(callSuper = true)  
    @Data  
    @Slf4j  
    public class ToolCallAgent extends ReActAgent {  
      
//        可用的工具
        private final ToolCallback[] availableTools;  
      
//        保存工具调用信息的响应结果
        private ChatResponse toolCallChatResponse;  
      
//        工具调用管理者
        private final ToolCallingManager toolCallingManager;  
      
//        禁用springai内置的工具调用机制，自己实现是否使用调用工具，维护选项和消息上下文
        private final ChatOptions chatOptions;  

//        构造函数
        public ToolCallAgent(ToolCallback[] availableTools, String modelName) {
            super();  
            this.availableTools = availableTools;  
            this.toolCallingManager = ToolCallingManager.builder().build();

            this.chatOptions = DashScopeChatOptions.builder()
                    // Prompt 级别的 ChatOptions 会覆盖 ChatModel 的默认选项，必须显式带上模型名。
                    .model(modelName)
                    // DeepSeek-V4.1-Flash 使用 DashScope 多模态生成端点。
                    .multiModel(true)
//                    这里把可使用的工具解包，框架在请求模型时提供工具名称、描述和参数格式
                    .toolCallbacks(Arrays.asList(availableTools))
//                    关闭框架自动执行工具
                    .internalToolExecutionEnabled(false)
                    .build();
        }
    /**
     * 让大模型分析当前对话，并判断下一步是否需要调用工具。
     *
     * @return 存在工具调用请求时返回 true，否则返回 false
     */
    @Override
    public boolean think() {
        // 如果设置了“下一步提示词”，就把它作为新的用户消息加入上下文。
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }

        // 这里必须使用 Spring AI 的 Message，不能使用阿里 DashScope SDK 中的同名 Message。
        List<Message> messageList = getMessageList();

        // 然后把完整上下文交给模型
//        原始用户问题；
//        之前模型的回答；
//        之前的工具调用；
//        工具执行结果；
//        当前的下一步提示。
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            // chatOptions 已经注册 availableTools，因此这里不需要再次调用 tools(...)。
            // internalToolExecutionEnabled=false 会让模型只返回工具调用请求，由 act() 手动执行。
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .call()
                    .chatResponse();

            // 保存本次响应，供 act() 读取模型选择的工具及参数。
            this.toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();

            // 读取模型的文本思考结果以及工具调用列表。
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info(getName() + "的思考: " + result);
            log.info(getName() + "选择了 " + toolCallList.size() + " 个工具来使用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s",
                            toolCall.name(),
                            toolCall.arguments())
                    )
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            if (toolCallList.isEmpty()) {
                // 没有工具调用时，将模型回复直接写入对话历史，本轮无需执行 act()。
                getMessageList().add(assistantMessage);
                return false;
            } else {
                // 存在工具调用时，由 ReActAgent.step() 继续调用 act()。
                return true;
            }
        } catch (Exception e) {
            // 记录完整异常堆栈，同时将错误信息加入对话历史。
            log.error(getName() + "的思考过程遇到了问题", e);
            // NonTransientAiException 等不可重试错误不应继续消耗剩余步骤。
            setState(AgentState.ERROR);
            getMessageList().add(
                    new AssistantMessage("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }

    /**
     * 执行 think() 阶段中模型请求调用的工具，并更新对话历史。
     *
     * @return 所有工具的执行结果摘要
     */
    @Override
    public String act() {
        // 防止 act() 被直接调用，或者模型响应中不存在工具调用。
        if (toolCallChatResponse == null || !toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }
//      调用工具
        // 使用调用模型时相同的上下文和配置，保证工具信息能够被正确解析。
        Prompt prompt = new Prompt(getMessageList(), chatOptions);

        // 执行模型请求的全部工具，并生成包含工具响应的新对话历史。
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);

        // 执行后，工具结果会被重新写入消息历史：
        setMessageList(toolExecutionResult.conversationHistory());

        // 拿到最新一条的消息
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
//        检查工具调用列表，如果有调用doTerminate函数就标记为终止
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (terminateToolCalled) {
//            切换agent的状态为FINISHED
            setState(AgentState.FINISHED);
        }
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 完成了它的任务！结果: " + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);
        return results;
    }


    }
