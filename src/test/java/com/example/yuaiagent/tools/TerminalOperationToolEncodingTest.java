package com.example.yuaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TerminalOperationToolEncodingTest {

    @Test
    void preservesChineseOutputFromWindowsCommandPrompt() {
        String result = new TerminalOperationTool()
                .executeTerminalCommand("echo 中文");

        assertTrue(result.contains("中文"), result);
    }
}
