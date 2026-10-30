package com.example.yuaiagent.rag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.ai.document.Document;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoveAppDocumentLoaderTest {

    @Test
    @DisplayName("没有状态后缀的文件不添加 status 元数据")
    void doesNotAddStatusMetadataWhenFilenameHasNoStatusSuffix() throws Exception {
        List<Document> documents = loadDocument("恋爱对象库.md");

        assertEquals(1, documents.size());
        assertEquals("恋爱对象库.md", documents.get(0).getMetadata().get("filename"));
        assertFalse(documents.get(0).getMetadata().containsKey("status"));
    }

    @ParameterizedTest
    @CsvSource({
            "恋爱常见问题和回答 - 单身篇.md, 单身",
            "恋爱常见问题和回答 - 已婚篇.md, 已婚",
            "恋爱常见问题和回答 - 恋爱篇.md, 恋爱"
    })
    @DisplayName("从状态篇文件名中提取 status 元数据")
    void addsStatusMetadataFromFilename(String fileName, String expectedStatus) throws Exception {
        List<Document> documents = loadDocument(fileName);

        assertEquals(1, documents.size());
        assertEquals(expectedStatus, documents.get(0).getMetadata().get("status"));
    }

    private List<Document> loadDocument(String fileName) throws Exception {
        Resource resource = new ByteArrayResource(
                "# 测试标题\n\n测试内容".getBytes(StandardCharsets.UTF_8)
        ) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        ResourcePatternResolver resolver = mock(ResourcePatternResolver.class);
        when(resolver.getResources("classpath:document/*.md"))
                .thenReturn(new Resource[]{resource});

        return new LoveAppDocumentLoader(resolver).loadMarkdowns();
    }
}
