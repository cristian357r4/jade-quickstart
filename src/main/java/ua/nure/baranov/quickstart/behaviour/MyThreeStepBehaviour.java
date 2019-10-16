package ua.nure.baranov.quickstart.behaviour;

import jade.core.behaviours.Behaviour;

public class MyThreeStepBehaviour extends Behaviour {
    private int step = 0;

    @Override
    public void action() {
        switch (step) {
            case 0:
                // do first operation
                step++;
                break;
            case 1:
                // do second operation
                step++;
                break;
            case 2:
                // do third operation
                step++;
                break;
            default:
                System.out.println("How did you get here?");
        }
    }

    @Override
    public boolean done() {
        return false;
    }
}
