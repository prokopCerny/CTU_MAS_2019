package student;

import mas.agents.Message;

public class MapMessage extends Message {
    public final int sourceAgentId;
    public final int type;
    public final int x;
    public final int y;

    public MapMessage(int type, int x, int y, int sourceAgentId) {
        super();
        this.type = type;
        this.x = x;
        this.y = y;
        this.sourceAgentId = sourceAgentId;
    }
}
