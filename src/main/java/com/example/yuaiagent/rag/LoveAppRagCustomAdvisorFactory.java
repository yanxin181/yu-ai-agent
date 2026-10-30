package com.example.yuaiagent.rag;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/*
* 创建自定义的rag检索增强顾问的工厂
* 可以实现以下的功能
* 设置相似度阈值
* 设置tok数量
* 设置关键词过滤
* */
public class LoveAppRagCustomAdvisorFactory {
    /*
    * 创建自定义的rag检索增强顾问
    * */
    public static Advisor createLoveAppRagCustomAdvisor(VectorStore vectorStore, String status) {
        Filter.Expression expression = new FilterExpressionBuilder()
//                需要是指定的status
                .eq("status", status)
                .build();
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
//                传给文档过滤器
                .filterExpression(expression)
//                相似度阈值
                .similarityThreshold(0.5)
//                返回文档的数量
                .topK(3)
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(LoveAppContextQuerAugmenterFactory.createInstance())
                .build();
    }
}
