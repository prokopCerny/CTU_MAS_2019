package student;

public abstract class AbstractStrategy implements Strategy {
    final Agent agent;

    public AbstractStrategy(Agent agent) {
        this.agent = agent;
    }
}
