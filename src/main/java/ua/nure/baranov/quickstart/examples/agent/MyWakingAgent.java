package ua.nure.baranov.quickstart.examples.agent;

import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;

public class MyWakingAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println("Adding waking behaviour...");
        addBehaviour(new WakerBehaviour(this, 10000L) {
            @Override
            protected void handleElapsedTimeout() {
                // do something
                // The operation will be started in 10 seconds after agent initialization.
            }
        });
    }
}
