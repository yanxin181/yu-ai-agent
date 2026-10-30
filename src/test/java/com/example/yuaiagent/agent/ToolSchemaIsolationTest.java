package com.example.yuaiagent.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ToolSchemaIsolationTest {

    @Resource
    private ChatModel dashscopeChatModel;

    @Test
    void baselineWithoutPromptOptions() {
        assertResponse(dashscopeChatModel.call(new Prompt("你好")));
    }

    @Test
    void dashScopeOptionsWithoutTools() {
        assertResponse(dashscopeChatModel.call(new Prompt("你好", multimodalOptions())));
    }

    @Test
    void onlyInternalToolExecutionDisabled() {
        var options = DashScopeChatOptions.builder()
                .multiModel(true)
                .internalToolExecutionEnabled(false)
                .build();
        assertResponse(dashscopeChatModel.call(new Prompt("你好", options)));
    }

    @Test
    void onlyEmptyToolCallbacks() {
        var options = DashScopeChatOptions.builder()
                .multiModel(true)
                .toolCallbacks(java.util.List.of())
                .build();
        assertResponse(dashscopeChatModel.call(new Prompt("你好", options)));
    }

    @Test
    void configuredDefaultOptions() {
        assertResponse(dashscopeChatModel.call(new Prompt(
                "你好", (DashScopeChatOptions) dashscopeChatModel.getDefaultOptions())));
    }

    private void assertResponse(org.springframework.ai.chat.model.ChatResponse response) {
        assertNotNull(response.getResult());
    }

    private DashScopeChatOptions multimodalOptions() {
        return DashScopeChatOptions.builder()
                .multiModel(true)
                .build();
    }
}
