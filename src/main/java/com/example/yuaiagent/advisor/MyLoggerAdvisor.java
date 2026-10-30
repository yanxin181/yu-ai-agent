//自定义日志拦截器
package com.example.yuaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {
    @Override
//    返回当前 Advisor 的名称MyLoggerAdvisor。
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
//    定义执行顺序
    public int getOrder() {
        return 0;
    }

//    请求前处理从请求中取得 Prompt。
//    从 Prompt 中取得用户消息。
//    取得用户消息的文本内容。
    private ChatClientRequest before(ChatClientRequest request) {
//        输出日志：AI Request: 请介绍一下 Spring AI
        log.info("AI Request: {}", request
                .prompt()
                .getUserMessage()
                .getText());
        return request;
    }

//    响应后处理 记录 AI 的最终响应
    private void observeAfter(ChatClientResponse response) {
        log.info("AI Response: {}",
                response
                .chatResponse()
                .getResult()
                        .getOutput()
                        .getText());
    }

//    普通调用处理，处理非流式调用，也就是等待 AI 生成完成后，一次性获得完整响应
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        request = this.before(request);
//        继续执行调用链
        ChatClientResponse response = chain.nextCall(request);
//        先记录 AI 的回答，然后将响应原样返回给上层调用者。
        this.observeAfter(response);
        return response;
    }

    @Override
//    流式调用处理
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        request = this.before(request);
        Flux<ChatClientResponse> responses = chain.nextStream(request);
//        聚合流式消息并记录
        return new ChatClientMessageAggregator()
                .aggregateChatClientResponse(responses, this::observeAfter);
    }
}
