package com.example.yuaiagent.agent;

import org.junit.jupiter.api.Test;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.tool.ToolCallback;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolCallAgentOptionsTest {

    @Test
    void usesMultimodalEndpointForConfiguredDeepSeekAgent() {
        ToolCallAgent agent = new ToolCallAgent(
                new ToolCallback[0],
                "deepseek-v4.1-flash-1017");

        DashScopeChatOptions options = (DashScopeChatOptions) agent.getChatOptions();
        assertTrue(options.getMultiModel());
    }
}
