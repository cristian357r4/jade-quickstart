package ua.nure.baranov.quickstart;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import ua.nure.baranov.quickstart.behaviour.OffsetRequestsServer;
import ua.nure.baranov.quickstart.behaviour.PurchaseOrdersServer;
import ua.nure.baranov.quickstart.ui.mock.BookSellerGui;

import java.util.HashMap;
import java.util.Map;

public class BookSeller extends Agent {

    private Map<String, Integer> catalogue;
    private BookSellerGui gui;

    @Override
    protected void setup() {
        catalogue = new HashMap<>();

        gui = new BookSellerGui(this);
        gui.show();

        addBehaviour(new OffsetRequestsServer(catalogue));
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
}
