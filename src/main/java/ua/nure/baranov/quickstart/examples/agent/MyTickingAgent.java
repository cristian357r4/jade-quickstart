package ua.nure.baranov.quickstart.examples.agent;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class MyTickingAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println("Adding ticking behaviour...");
        addBehaviour(new TickerBehaviour(this, 10000L) {
            @Override
            protected void onTick() {
                // do something
                // The operation will be performed every 10 seconds (when tick occurs).
            }
        });
    }
}
