package com.example.yuaiagent.rag;

import org.junit.jupiter.api.Assertions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Import(PgVectorVectorStoreConfig.class)
class PgVectorVectorStoreConfigTest {

    @Resource
    private VectorStore pgVectorVectorStore;

    @Test
    void test() {
        List<Document> documents = List.of(
                new Document("鱼皮的编程导航有什么用", Map.of("meta1", "meta1")),
                new Document("鱼皮原创项目教程 codefather.cn"),
                new Document("鱼皮小帅", Map.of("meta2", "meta2")));
//        添加文档
        pgVectorVectorStore.add(documents);
//      相似度查询
        List<Document> results = pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("怎么学编程").topK(5).build());
        Assertions.assertNotNull(results);
    }

}