package ua.nure.baranov.quickstart;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import ua.nure.baranov.quickstart.behaviour.RequestPerformer;

public class BookBuyer extends Agent {

    private String targetBookTitle;

    private AID[] sellerAgents = {
            new AID("seller1", AID.ISLOCALNAME),
            new AID("seller2", AID.ISLOCALNAME)
    };

    @Override
    protected void setup() {
        System.out.println("Hello there! My name is " + getAID().getName());
        Object[] arguments = getArguments();
        if (arguments != null && arguments.length != 0) {
            targetBookTitle = (String) arguments[0];
            System.out.println("I'll try to buy " + targetBookTitle);

            addBehaviour(new TickerBehaviour(this, 60000) {
                @Override
                protected void onTick() {
                    myAgent.addBehaviour(new RequestPerformer(sellerAgents, targetBookTitle));
                }
            });
        } else {
            System.out.println("Don't have anything to buy. Bye!");
            doDelete();
        }
    }

    @Override
    protected void takeDown() {
        System.out.println("Agent " + getAID().getName() + " is shutting down");
    }
}
