package student;

import mas.agents.task.mining.StatusMessage;

import java.io.IOException;

public interface Strategy {
    boolean canStop();

    StatusMessage act(StatusMessage status) throws Exception;
}
