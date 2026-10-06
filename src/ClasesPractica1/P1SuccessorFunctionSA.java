package ClasesPractica1;

import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class P1SuccessorFunctionSA implements SuccessorFunction {

    /**
     * Generates a random successor for Simulated Annealing
     * @param state current state whose successor is to be generated
     * @return a list with one element, the randomly generated successor
     */
    @Override
    public List getSuccessors(Object state) throws Exception {
        ArrayList<Successor> successors = new ArrayList<>();
        P1State currentState = (P1State) state;
        P1HeuristicFunction h = new P1HeuristicFunction();
        Random r = new  Random();
        int clientID = r.nextInt(currentState.getClients().size());
        int currentPowerPlantID = currentState.getAssignment()[clientID];

        P1State successorState = new P1State(currentState.getClients(), currentState.getPowerPlants());
        successorState.setAssignment(currentState.getAssignment().clone());
        int  newPowerPlantID = r.nextInt(currentState.getPowerPlants().size() +1) - 1;

        //make sure the successor does not have the same assignment
        while(!successorState.changeAssignment(clientID, newPowerPlantID)){
            newPowerPlantID = r.nextInt(currentState.getPowerPlants().size() +1) - 1;
        }
        double cost = h.getHeuristicValue(successorState);
        String action = "Change Assignment Client " + clientID + ": power plant " +
                currentPowerPlantID + " ---> " + newPowerPlantID + " Cost: " + cost;
        successors.add(new Successor(action, successorState));
        return successors;
    }
}
