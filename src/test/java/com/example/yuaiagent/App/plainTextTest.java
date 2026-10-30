package com.example.yuaiagent.App;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class plainTextTest {

    @Resource
    private ChatModel dashscopeChatModel;

    @Test
    void plainTextTest() {
        var response = dashscopeChatModel.call(new Prompt("你好"));
        assertNotNull(response.getResult());
        String text = response.getResult().getOutput().getText();
        assertNotNull(text);
        assertFalse(text.isBlank());
        System.out.println("Direct DashScope response: " + text);
    }
}
