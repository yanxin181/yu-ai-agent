package com.example.yuaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/*
* 恋爱大师向量数据库配置 初始化基于内存的向量数据库bean
* */
@Configuration
public class LoveAppVectorStoreConfig {


    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

//    引入关键词增强器
    @Resource
    private MyKeywordEnricher myKeywordEnricher;
    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
//        创建内存向量库
//        SimpleVectorStore 是一个比较简单的内存向量库
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();
//      加载md文档
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();

//        加载文档后进行切词操作（不建议使用这个切词器，可以使用云服务的智能切分）
//        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documents);

//        自动补充关键词元信息
        List<Document> enrichedDocuments =myKeywordEnricher.enrichDocuments(documents);
//        把文档加入向量库
        simpleVectorStore.add(enrichedDocuments);
        return simpleVectorStore;
    }
}
