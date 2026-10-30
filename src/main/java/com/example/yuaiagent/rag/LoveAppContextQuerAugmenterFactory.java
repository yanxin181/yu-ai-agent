package com.example.yuaiagent.rag;

/*
* 创建上下文查询增强器工厂
* 处理一些边界情况
* 如果有问题ai不能回答（知识库里面没有）就联系客服
* */

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

public class LoveAppContextQuerAugmenterFactory {
    public static ContextualQueryAugmenter createInstance() {
        PromptTemplate emptyContextPromptTemplate = new PromptTemplate("""
                    你应该输出下面的内容：
                    抱歉，我只能回答恋爱相关的问题，别的没办法帮到您哦，
                    有问题可以联系编程导航客服 https://codefather.cn
                    """);
        return ContextualQueryAugmenter.builder()
//                检索不到就只想下面的内容呢，如果是true就利用ai大模型自己的能力回答
                .allowEmptyContext(false)
//                空提示词模板改成上面自己设置的
                .emptyContextPromptTemplate(emptyContextPromptTemplate)
                .build();
    }
}
