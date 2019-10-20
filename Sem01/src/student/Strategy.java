package student;

import mas.agents.task.mining.StatusMessage;

import java.io.IOException;

public interface Strategy {
    StatusMessage act(StatusMessage status) throws Exception;

    void handleMessage(AgentMessage m) throws Exception;
}
