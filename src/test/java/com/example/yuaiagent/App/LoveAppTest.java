package com.example.yuaiagent.App;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppTest {


    @Resource
    private LoveApp loveApp;

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
//        第一轮
        String message = "你好我是程序员鱼皮";
        String answer = loveApp.doChat(message,chatId);
//        第二轮
        message = "我想有一个灵魂伴侣";
        answer = loveApp.doChat(message,chatId);
        Assertions.assertNotNull(answer);
//        第三轮
        message = "回忆一下我的目标";
        answer = loveApp.doChat(message,chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
//        第一轮
        String message = "你好我是程序员鱼皮，我想有一个灵魂伴侣，但我不知道怎么做";
        LoveApp.LoveReport loveReport= loveApp.doChatWithReport(message,chatId);
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
//        第一轮
        String message = "我已经结婚了，婚后关系不和睦怎么办";
        String answer = loveApp.doChatWithRag(message,chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithRagRecommend() {
        String chatId = UUID.randomUUID().toString();
//        第一轮
        String message = "我的性格比较安静，喜欢小猫小狗，请给我推荐一个恋爱人选";
        String answer = loveApp.doChatWithRagRecommend(message,chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {

        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");


        testMessage("最近和对象吵架了，看看编程导航网站（codefather.cn）的其他情侣是怎么解决矛盾的？");


        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");


        testMessage("执行 Python3 脚本来生成数据分析报告");


        testMessage("保存我的恋爱档案为文件");


        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
//        测试地图mcp
        String chatId = UUID.randomUUID().toString();
        String message = "帮我找河海大学江宁校区5公里内适合约会的场所";
        String answer = loveApp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);

//        测试图片搜索mcp
//        String chatId = UUID.randomUUID().toString();
//        String message = "帮我找桂花的图片";
//        String answer = loveApp.doChatWithMcp(message, chatId);
//        Assertions.assertNotNull(answer);
    }
}