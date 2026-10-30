package com.example.yuaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.stereotype.Component;

import java.util.List;

/*
* 查询拓展（把一个问题拓展成几个更具体的问题）
* */
@Component
public class MultiQueryExpanderDemo {
    @Resource
    private ChatClient.Builder chatClientBuilder;
    public List<Query> expand(String query){
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClientBuilder)
                .numberOfQueries(3)
                .build();
        List<Query> queries = queryExpander.expand(new Query("谁是程序员鱼皮啊？"));
        return queries;
    }
}
