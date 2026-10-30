package com.example.yuaiagent.agent;

import com.example.yuaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
    public class YuManus extends ToolCallAgent {  

//      通过构造方法把工具传给YuManus
        public YuManus(ToolCallback[] allTools,
                       ChatModel dashscopeChatModel,
                       @Value("${spring.ai.dashscope.chat.options.model:qwen-plus}") String modelName) {
            super(allTools, modelName);
            this.setName("yuManus");
//            你是 YuManus，一款全能型人工智能助手，旨在完成用户提出的各类任务。你配备多种工具，可调用它们高效处理复杂需求。
            String SYSTEM_PROMPT = """  
                    You are YuManus, an all-capable AI assistant, aimed at solving any task presented by the user.  
                    You have various tools at your disposal that you can call upon to efficiently complete complex requests.  
                    """;  
            this.setSystemPrompt(SYSTEM_PROMPT);
//            根据用户需求，主动选择最合适的工具或工具组合。对于复杂任务，你可以拆解问题，分步调用不同工具进行解决。
//            每次调用工具后，清晰说明执行结果，并给出下一步建议。
//            若你需要在任意时刻终止交互，请调用 `terminate` 工具 / 函数。
            String NEXT_STEP_PROMPT = """  
                    Based on user needs, proactively select the most appropriate tool or combination of tools.  
                    For complex tasks, you can break down the problem and use different tools step by step to solve it.  
                    After using each tool, clearly explain the execution results and suggest the next steps.  
                    If you want to stop the interaction at any point, use the `terminate` tool/function call.  
                    """;  
            this.setNextStepPrompt(NEXT_STEP_PROMPT);  
            this.setMaxSteps(20);  

//            初始化ai对话客户端
            ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                    .defaultAdvisors(new MyLoggerAdvisor())
                    .build();  
            this.setChatClient(chatClient);  
        }  
    }
