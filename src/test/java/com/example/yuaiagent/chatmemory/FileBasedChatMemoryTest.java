package com.example.yuaiagent.chatmemory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.chat.messages.UserMessage;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileBasedChatMemoryTest {

    @TempDir
    Path directory;

    @Test
    void retrievesLastTenMessagesWithoutDiscardingStoredHistory() {
        FileBasedChatMemory memory = new FileBasedChatMemory(directory.toString(), 10);
        for (int i = 1; i <= 12; i++) {
            memory.add("conversation", new UserMessage("message-" + i));
        }

        assertEquals(List.of("message-3", "message-4", "message-5", "message-6",
                "message-7", "message-8", "message-9", "message-10", "message-11", "message-12"),
                memory.get("conversation").stream().map(message -> message.getText()).toList());

        FileBasedChatMemory reopened = new FileBasedChatMemory(directory.toString());
        assertEquals(12, reopened.get("conversation", 12).size());
        assertEquals("message-1", reopened.get("conversation", 12).get(0).getText());
    }
}
