package student;

public abstract class AbstractStrategy implements Strategy {
    final Agent agent;
    protected boolean canChange = true;

    public AbstractStrategy(Agent agent) {
        this.agent = agent;
    }

    @Override
    public final boolean canChange() {
        return canChange;
    }
}
