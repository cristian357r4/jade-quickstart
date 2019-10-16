package ua.nure.baranov.quickstart;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
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

        addBehaviour(new OffsetRequestsServer());
        addBehaviour(new PurchaseOrdersServer());
    }

    @Override
    protected void takeDown() {
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
