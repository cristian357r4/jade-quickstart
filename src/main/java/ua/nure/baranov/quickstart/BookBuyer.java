package ua.nure.baranov.quickstart;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import ua.nure.baranov.quickstart.behaviour.RequestPerformer;

import java.util.Arrays;

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
                    final DFAgentDescription templateDescription = new DFAgentDescription();
                    final ServiceDescription serviceDescription = new ServiceDescription();

                    serviceDescription.setType("book-selling");
                    templateDescription.addServices(serviceDescription);

                    try {
                        final DFAgentDescription[] result = DFService.search(myAgent, templateDescription);
                        sellerAgents = Arrays.stream(result)
                                .map(DFAgentDescription::getName)
                                .toArray(AID[]::new);

                    } catch (FIPAException e) {
                        e.printStackTrace();
                    }
                }
            });

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
