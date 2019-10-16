package ua.nure.baranov.quickstart;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ua.nure.baranov.quickstart.ui.BookSellerGui;

import java.util.HashMap;
import java.util.Map;

public class BookSeller extends Agent {
    private static final String BOOK_NOT_AVAILABLE_MESSAGE = "not-available";
    private static final MessageTemplate CFP_MESSAGE_TEMPLATE = MessageTemplate.MatchPerformative(ACLMessage.CFP);

    private Map<String, Integer> catalogue;
    private BookSellerGui gui;

    @Override
    protected void setup() {
        catalogue = new HashMap<>();

        gui = new BookSellerGui(this);
        gui.showGui();

        addBehaviour(new OffsetRequestsServer());
        addBehaviour(new PurchaseOrdersServer());

        // Register the agent!

        final DFAgentDescription description = new DFAgentDescription();
        description.setName(this.getAID());
        final ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType("book-selling");
        serviceDescription.setName("JADE-quickstart-book-seller");
        description.addServices(serviceDescription);

        try {
            DFService.register(this, description);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        gui.dispose();
        System.out.println("Book seller, ID " + this.getAID().getName() + ", going down");
    }

    public void updateCatalogue(String title, int price) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                catalogue.put(title, price);
            }
        });
    }

    private class PurchaseOrdersServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String title = msg.getContent();
                ACLMessage reply = msg.createReply();

                Integer price = catalogue.remove(title);
                if (price != null) {
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(title+" sold to agent "+msg.getSender().getName());
                }
                else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            }
            else {
                block();
            }
        }
    }

    private class OffsetRequestsServer extends CyclicBehaviour {

        @Override
        public void action() {
            final ACLMessage message = myAgent.receive(CFP_MESSAGE_TEMPLATE);
            if (message != null) {
                final String title = message.getContent();
                final ACLMessage reply = message.createReply();

                final Integer price = catalogue.get(title);
                if (price != null) {
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(price.toString());
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent(BOOK_NOT_AVAILABLE_MESSAGE);
                }
            } else {
                // We don't have the message yet, stop consuming the CPU resources and just wait for one.
                block();
            }
        }
    }

}
