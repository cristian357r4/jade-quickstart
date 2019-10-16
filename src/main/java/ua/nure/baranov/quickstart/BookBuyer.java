package ua.nure.baranov.quickstart;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Arrays;

import static jade.lang.acl.MessageTemplate.MatchConversationId;
import static jade.lang.acl.MessageTemplate.MatchReplyWith;

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
                    myAgent.addBehaviour(new RequestPerformer());
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


    private class RequestPerformer extends Behaviour {
        private static final String BOOK_TRADE_CONVERSATION_ID = "book-trade";
        private AID bestSeller;
        private int bestPrice;
        private int countOfReplies = 0;
        private int step = 0;
        private MessageTemplate template;

        @Override
        public void action() {
            switch (step) {
                case 0:
                    final ACLMessage callForProposal = new ACLMessage(ACLMessage.CFP);
                    for (AID sellerAgent : sellerAgents) {
                        callForProposal.addReceiver(sellerAgent);
                    }
                    callForProposal.setContent(targetBookTitle);
                    callForProposal.setConversationId(BOOK_TRADE_CONVERSATION_ID);
                    callForProposal.setReplyWith(String.format("cfp %d", System.currentTimeMillis()));
                    myAgent.send(callForProposal);
                    template = MessageTemplate.and(MatchConversationId(BOOK_TRADE_CONVERSATION_ID),
                            MatchReplyWith(callForProposal.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    final ACLMessage reply = myAgent.receive(template);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            final int price = Integer.parseInt(reply.getContent());
                            if (bestSeller == null || price < bestPrice) {
                                bestPrice = price;
                                bestSeller = reply.getSender();
                            }
                        }
                        countOfReplies++;
                        if (countOfReplies >= sellerAgents.length) {
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    final ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller);
                    order.setContent(targetBookTitle);
                    order.setConversationId(BOOK_TRADE_CONVERSATION_ID);
                    order.setReplyWith(String.format("order %d", System.currentTimeMillis()));
                    myAgent.send(order);

                    template = MessageTemplate.and(MatchConversationId(BOOK_TRADE_CONVERSATION_ID),
                            MatchReplyWith(order.getReplyWith()));
                    step = 3;
                    break;
                case 3:
                    final ACLMessage orderReply = myAgent.receive(template);
                    if (orderReply != null) {
                        if (orderReply.getPerformative() == ACLMessage.INFORM) {
                            System.out.println("We have successfully bought a book " + targetBookTitle);
                            System.out.println("Final price: " + bestPrice);
                            myAgent.doDelete();
                        }
                        step = 4;
                    } else {
                        block();
                    }
                    break;
            }
        }

        @Override
        public boolean done() {
            return ((step == 2 && bestSeller == null) || step == 4);
        }
    }
}
