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
    final Random random = new Random(43);
    private Position depot = null;

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


        while(true) {
            while (messageAvailable()) {
                Message m = readMessage();
                if (m instanceof MapMessage) {
                    MapMessage M = (MapMessage) m;
                    switch (M.type) {
                        case StatusMessage.DEPOT:
                            depot = new Position(M.x, M.y);
                            break;
                        default:
                            log("I have received " + M);
                            break;
                    }
                } else {
                    log("I have received " + m);
                }
            }

            if (depot == null) {
                StatusMessage status;
                switch (random.nextInt(4)) {
                    case 0:
                        status = left();
                        break;
                    case 1:
                        status = right();
                        break;
                    case 2:
                        status = up();
                        break;
                    default:
                        status = down();
                        break;
                }
                if (status.isAtGold()) {
                    status = pick();
                }
                log(String.format("I am now on position [%d,%d] of a %dx%d map.",
                        status.agentX, status.agentY, status.width, status.height));
                for(StatusMessage.SensorData data : status.sensorInput) {
                    log(String.format("I see %s at [%d,%d]", types[data.type], data.x, data.y));
                    if (data.type == StatusMessage.DEPOT) {
                        depot = new Position(data.x, data.y);
                        for (int agent = 1; agent <= 4; agent++) {
                            if (agent != getAgentId()) {
                                log(String.format("Sending depo to %d", agent));
                                sendMessage(agent, new MapMessage(data.type, data.x, data.y));
                            }
                        }
                    }
                }

            } else {
                log("Going to depo!");
                StatusMessage status = sense();
                if (Math.abs(status.agentX - depot.x) > Math.abs(status.agentY - depot.y)) {
                    if (status.agentX > depot.x) {
                        status = left();
                    } else if (status.agentX < depot.x){
                        status = right();
                    }
                } else {
                    if (status.agentY > depot.y) {
                        status = up();
                    } else if (status.agentY < depot.y){
                        status = down();
                    }
                }

            }




            // REMOVE THIS BEFORE SUBMITTING YOUR SOLUTION TO BRUTE !!
            //   (this is meant just to slow down the execution a bit for demonstration purposes)
//            try {
//                Thread.sleep(2000);
//            } catch(InterruptedException ie) {}
        }
    }
}
