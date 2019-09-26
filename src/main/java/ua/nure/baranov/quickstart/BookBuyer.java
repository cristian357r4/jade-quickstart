package ua.nure.baranov.quickstart;

import jade.core.Agent;

public class BookBuyer extends Agent {

    @Override
    protected void setup() {
        System.out.println("Hello there! My name is " + getAID().getName());
    }
}
