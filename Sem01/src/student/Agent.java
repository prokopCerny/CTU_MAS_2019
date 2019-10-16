package student;

import mas.agents.AbstractAgent;
import mas.agents.Message;
import mas.agents.SimulationApi;
import mas.agents.StringMessage;
import mas.agents.task.mining.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

public class Agent extends AbstractAgent {
    Position depot = null;
    Strategy strategy = new RandomWalkStrategy(this);
    private Map map;
    private long time = 1L;

    public Agent(int id, InputStream is, OutputStream os, SimulationApi api) throws IOException, InterruptedException {
        super(id, is, os, api);
    }

    // See also class StatusMessage
    public static String[] types = {
            "", "obstacle", "depot", "gold", "agent"
    };

    @Override
    public void act() throws Exception {
        for (int curId = getAgentId()-1; curId > 0; --curId) {
            sendMessage(curId, new StringMessage(String.format("I, %d exist", getAgentId())));
        }
        StatusMessage start = sense();
        map = new Map(start.width, start.height);
        for (StatusMessage.SensorData d : start.sensorInput) {
            if(map.update(d.x, d.y, d.type, time)) {
                for (int agentId = 1; agentId <= 5; agentId++) {
                    if (agentId != getAgentId()) {
                        sendMessage(agentId, new MapMessage(d.type, d.x, d.y));
                    }
                }
            }
        }


        while(true) {
            time++;
            while (messageAvailable()) {
                Message m = readMessage();
                handleMessage(m);
            }

            strategy.act(sense());



            // REMOVE THIS BEFORE SUBMITTING YOUR SOLUTION TO BRUTE !!
            //   (this is meant just to slow down the execution a bit for demonstration purposes)
//            try {
//                Thread.sleep(2000);
//            } catch(InterruptedException ie) {}
        }
    }

    void handleMessage(Message m) throws Exception {
        if (m instanceof MapMessage) {
            MapMessage M = (MapMessage) m;
            map.update(M, time);
            switch (M.type) {
                case StatusMessage.DEPOT:
                    depot = new Position(M.x, M.y);
                    strategy = new GoToDepotStrategy(this);
                    break;
                default:
                    log("I have received " + M);
                    break;
            }
        } else {
            log("I have received " + m);
        }
    }
}
