package com.example.yuaiagent.demo.invoke;// 该代码 OpenAI SDK 版本为 2.6.0
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
/*
* 阿里云灵机sdk调用
* */
public class SdkAiInvoke {
    public static void main(String[] args) {
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "请先设置环境变量 DASHSCOPE_API_KEY"
            );
        }
        OpenAIClient client = OpenAIOkHttpClient.builder()

                .apiKey(apiKey)
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .addUserMessage("你是谁")
                .model("deepseek-v4-flash-0731")
                .build();

        try {
            ChatCompletion chatCompletion = client.chat().completions().create(params);
            System.out.println(chatCompletion);
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}