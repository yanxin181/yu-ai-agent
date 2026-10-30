package com.example.yuaiagent.chatmemory;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import com.esotericsoftware.kryo.io.Input;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.io.File;
import java.util.List;

public class FileBasedChatMemory implements ChatMemory {
    private  final String BASE_DIR;
    private final int maxMessages;

    private static  final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

//    构造对象时指定文件保存目录
    public FileBasedChatMemory(String dir){
        this(dir, Integer.MAX_VALUE);
    }

    public FileBasedChatMemory(String dir, int maxMessages){
        if (maxMessages <= 0) {
            throw new IllegalArgumentException("maxMessages must be greater than 0");
        }
        this.BASE_DIR = dir;
        this.maxMessages = maxMessages;
        File baseDir = new File(dir);
        if(!baseDir.exists()){
            baseDir.mkdirs();
        }
    }

    @Override

    public void add(String conversationId, Message message) {
        add(conversationId, List.of(message));
    }

    @Override
    //    往指定对象插入
    public void add(String conversationId, List<Message> messages) {
//        从文件中读取所有消息
        List<Message> conversationMessages = getOrCreateConversation(conversationId);
        conversationMessages.addAll(messages);
        saveConversation(conversationId, conversationMessages);

    }

    @Override
    public List<Message> get(String conversationId) {
        return get(conversationId, maxMessages);
    }
    public List<Message> get(String conversationId, int lastN) {
        List<Message> allMessages = getOrCreateConversation(conversationId);
        return allMessages.stream()
                .skip(Math.max(0, allMessages.size() - lastN))
                .toList();
    }


    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if(file.exists()){
            file.delete();
        }
    }

    /*
    * 获取或创建会话消息的列表
    * */
    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if (file.exists()) {
            try (Input input = new Input(new FileInputStream(file))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
            return messages;
    }

    /*
    * 保存对话消息
    * */
    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * 每个会话文件单独保存
     * */
    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR,conversationId + ".kryo");

    }
}
