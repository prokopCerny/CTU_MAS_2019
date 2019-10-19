package student;

import mas.agents.task.mining.StatusMessage;

import java.io.IOException;

public interface Strategy {
    boolean canChange();

    StatusMessage act(StatusMessage status) throws Exception;

    void handleMessage(AgentMessage m) throws Exception;
}
