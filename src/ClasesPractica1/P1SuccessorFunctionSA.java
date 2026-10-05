package ClasesPractica1;

import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class P1SuccessorFunctionSA implements SuccessorFunction {

    @Override
    public List getSuccessors(Object state) {
        ArrayList<P1State> successors = new ArrayList<>();
        P1State currentState = (P1State) state;
        Random r = new  Random();
        P1State successor = new P1State(currentState.getClients(), currentState.getPowerPlants());
        successor.setAssignment(currentState.getAssignment().clone());
        int clientID = r.nextInt(currentState.getClients().size() +1);
        int  powerPlantID = r.nextInt(currentState.getPowerPlants().size() +1) - 1;
        successor.changeAssignment(clientID, powerPlantID);
        successors.add(successor);
        System.out.println(successor);
        return successors;
    }
}
