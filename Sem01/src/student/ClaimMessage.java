package student;

import mas.agents.Message;

public class ClaimMessage extends Message {
    final int x;
    final int y;
    public ClaimMessage(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }
}
