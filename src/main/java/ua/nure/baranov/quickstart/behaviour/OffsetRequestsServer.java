package ua.nure.baranov.quickstart.behaviour;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Map;

public class OffsetRequestsServer extends CyclicBehaviour {

    private static final String BOOK_NOT_AVAILABLE_MESSAGE = "not-available";
    private static final MessageTemplate CFP_MESSAGE_TEMPLATE = MessageTemplate.MatchPerformative(ACLMessage.CFP);
    private Map<String, Integer> catalogue;

    public OffsetRequestsServer(Map<String, Integer> catalogue) {
        this.catalogue = catalogue;
    }

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
