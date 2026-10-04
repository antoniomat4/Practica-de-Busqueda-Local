package ClasesPractica1;

import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class P1SuccessorFunctionSA implements SuccessorFunction {

    @Override
    public List getSuccessors(Object state) {
        ArrayList<State> successors = new ArrayList<>();
        State currentState = (State) state;
        Random r = new  Random();
        State successor = new State(currentState.getClients(), currentState.getPowerPlants());
        successor.setAssignment(currentState.getAssignment().clone());
        int clientID = r.nextInt(currentState.getClients().size() +1);
        int  powerPlantID = r.nextInt(currentState.getPowerPlants().size() +2) - 1;
        successor.changeAssignment(clientID, powerPlantID);
        successors.add(successor);
        System.out.println(successor);
        return successors;
    }
}
