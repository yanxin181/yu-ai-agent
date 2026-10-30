package com.example.yuaiagent.agent;

import org.springframework.ai.chat.messages.Message;
import com.example.yuaiagent.agent.model.AgentState;
import com.itextpdf.styledxmlparser.jsoup.internal.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


/*
* 主要控制整个循环，就是个循环框架
* 检查 Agent 是否空闲；
* 保存用户问题；
* 把状态改为 RUNNING；
* 循环执行 step()；
* 最多循环 20 次；
* 直到状态变成 FINISHED 或 ERROR。
* */
@Data
@Slf4j
public abstract class BaseAgent {


    private String name;


    private String systemPrompt;
    private String nextStepPrompt;


    private AgentState state = AgentState.IDLE;


    private int maxSteps = 10;
    private int currentStep = 0;

//llm模型
    private ChatClient chatClient;

//上下文记忆（自主维护上下文）
    private List<Message> messageList = new ArrayList<>();


//    整体流程运行大模型，需要传入用户提示词
    public String run(String userPrompt) {
//        基础校验
//        如果不是空闲状态就抛出异常
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
//        如果用户提示词为空
        if (StringUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }

//      更新状态为运行中
        state = AgentState.RUNNING;

        messageList.add(new UserMessage(userPrompt));

//        定义一个结果列表
        List<String> results = new ArrayList<>();
        try {
//            执行循环
//            判断当前不超过最大步数，并且状态不是结束
            for (int i = 0; i < maxSteps
                    && state != AgentState.FINISHED
                    && state != AgentState.ERROR; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
//                打印一下执行结果
                log.info("Executing step " + stepNumber + "/" + maxSteps);
//              单步执行
                String stepResult = step();
//              对结果进行处理
                String result = "Step " + stepNumber + ": " + stepResult;
//              添加到执行列表
                results.add(result);
            }
//          如果结束就把状态改成结束“FINISHED”
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
//          清理资源
            this.cleanup();
        }
    }
/*
* 流式输出
* */
//  每完成一步就流式的向前端推送
public SseEmitter runStream(String userPrompt) {

    SseEmitter emitter = new SseEmitter(300000L);


    CompletableFuture.runAsync(() -> {
        try {
            if (this.state != AgentState.IDLE) {
                emitter.send("错误：无法从状态运行代理: " + this.state);
                emitter.complete();
                return;
            }
            if (StringUtil.isBlank(userPrompt)) {
                emitter.send("错误：不能使用空提示词运行代理");
                emitter.complete();
                return;
            }


            state = AgentState.RUNNING;

            messageList.add(new UserMessage(userPrompt));

            try {
                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("Executing step " + stepNumber + "/" + maxSteps);


                    String stepResult = step();
                    String result = "Step " + stepNumber + ": " + stepResult;


                    emitter.send(result);
                }

                if (currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    emitter.send("执行结束: 达到最大步骤 (" + maxSteps + ")");
                }
//                正常完成
                emitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("执行智能体失败", e);
                try {
                    emitter.send("执行错误: " + e.getMessage());
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            } finally {

                this.cleanup();
            }
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    });

//设置超时回调
    emitter.onTimeout(() -> {
//        更改状态为error
        this.state = AgentState.ERROR;
        this.cleanup();
        log.warn("SSE connection timed out");
    });

    emitter.onCompletion(() -> {
        if (this.state == AgentState.RUNNING) {
            this.state = AgentState.FINISHED;
        }
        this.cleanup();
        log.info("SSE connection completed");
    });

    return emitter;
}


// 定义单个步骤
    public abstract String step();


//    清理资源
    protected void cleanup() {

    }
}
