package com.example.yuaiagent.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AgentTerminationTest {

    @Test
    void finishesWhenTheModelHasNoFurtherAction() {
        NoActionAgent agent = new NoActionAgent();
        agent.setMaxSteps(20);

        String result = agent.run("完成一个简单任务");

        assertEquals(1, agent.thinkCalls);
        assertFalse(result.contains("Reached max steps"), result);
    }

    @Test
    void completedAgentCanStartAnotherTask() {
        NoActionAgent agent = new NoActionAgent();

        assertDoesNotThrow(() -> agent.run("第一个任务"));
        assertDoesNotThrow(() -> agent.run("第二个任务"));

        assertEquals(2, agent.thinkCalls);
    }

    private static final class NoActionAgent extends ReActAgent {
        private int thinkCalls;

        @Override
        public boolean think() {
            thinkCalls++;
            return false;
        }

        @Override
        public String act() {
            throw new AssertionError("No-action agent should not execute act()");
        }
    }
}
