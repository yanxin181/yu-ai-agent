package com.example.yuaiagent.App;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoveAppStreamingApiTest {

    @Test
    void exposesStreamingChatMethod() throws NoSuchMethodException {
        Method method = LoveApp.class.getMethod("doChatByStream", String.class, String.class);

        assertEquals(Flux.class, method.getReturnType());
    }
}
