package ua.nure.baranov.quickstart.behaviour;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import static jade.lang.acl.MessageTemplate.MatchConversationId;
import static jade.lang.acl.MessageTemplate.MatchReplyWith;

public class RequestPerformer extends Behaviour {
    private static final String BOOK_TRADE_CONVERSATION_ID = "book-trade";
    private final String targetBookTitle;
    private final AID[] sellerAgents;
    private AID bestSeller;
    private int bestPrice;
    private int countOfReplies = 0;
    private int step = 0;
    private MessageTemplate template;

    public RequestPerformer(AID[] sellerAgents, String targetBookTitle) {
        this.sellerAgents = sellerAgents;
        this.targetBookTitle = targetBookTitle;
    }

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
