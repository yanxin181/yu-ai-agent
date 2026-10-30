package com.example.yuaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScrapingToolTest {

    @Test
    public void testScrapeWebPage() {
        WebScrapingTool tool = new WebScrapingTool();
        String url = "https://www.codefather.cn";
        String result = tool.scrapeWebPage(url);
        assertNotNull(result);
    }
}