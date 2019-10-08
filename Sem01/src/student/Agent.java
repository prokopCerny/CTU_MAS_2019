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

    public Agent(int id, InputStream is, OutputStream os, SimulationApi api) throws IOException, InterruptedException {
        super(id, is, os, api);
    }

    // See also class StatusMessage
    public static String[] types = {
            "", "obstacle", "depot", "gold", "agent"
    };

    @Override
    public void act() throws Exception {
        sendMessage(1, new StringMessage("Hello"));

        while(true) {
            while (messageAvailable()) {
                Message m = readMessage();
                log("I have received " + m);
            }

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
            }


            // REMOVE THIS BEFORE SUBMITTING YOUR SOLUTION TO BRUTE !!
            //   (this is meant just to slow down the execution a bit for demonstration purposes)
//            try {
//                Thread.sleep(200);
//            } catch(InterruptedException ie) {}
        }
    }
}
