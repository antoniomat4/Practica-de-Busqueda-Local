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
        String action;

        P1State successorState = new P1State(currentState.getClients(), currentState.getPowerPlants());
        successorState.setAssignment(currentState.getAssignment().clone());
        successorState.setCurrentProduction(currentState.getCurrentProduction().clone());

        int operatorChoice = r.nextInt(2);
        // get successor with the changeAssignment operator
        if(operatorChoice == 0) {
            int clientID = r.nextInt(currentState.getClients().size());
            int currentPowerPlantID = currentState.getAssignment()[clientID];
            int newPowerPlantID = r.nextInt(currentState.getPowerPlants().size() + 1) - 1;
            if (successorState.changeAssignment(clientID, newPowerPlantID)) {
                double cost = h.getHeuristicValue(successorState);
                action = "Change Assignment Client " + clientID + ": power plant " +
                        currentPowerPlantID + " ---> " + newPowerPlantID + "; Cost: " + cost;
            } else {
                double cost = h.getHeuristicValue(successorState);
                action = "Change assignment unsuccessful. No changes; Cost: " + cost;
            }
            successors.add(new Successor(action, successorState));
        } else {  // get Successor with the swapAssignment operator
            int clientID1 = r.nextInt(currentState.getClients().size());
            int clientID2 = r.nextInt(currentState.getClients().size());
            int powerPlantID1 = currentState.getAssignment()[clientID1];
            int powerPlantID2 = currentState.getAssignment()[clientID2];
            if(successorState.swapAssignment(clientID1, clientID2)){
                double cost =  h.getHeuristicValue(successorState);
                action = "Swap Assignment Client " + clientID1 + ": " + powerPlantID1 + " ---> " + powerPlantID2
                        + "; Client " + clientID2 + ": " + powerPlantID2 + " ---> " + powerPlantID1 + "; Cost: " + cost;
            }else{
                double cost = h.getHeuristicValue(successorState);
                action = "Swap assignment unsuccessful. No changes; Cost: " + cost;
            }
        }
        //
//        else {
//            int clientID1 = r.nextInt(currentState.getClients().size());
//            int clientID2 = r.nextInt(currentState.getClients().size());
//            int clientID3 = r.nextInt(currentState.getClients().size());
//            int powerPlantID1 = currentState.getAssignment()[clientID1];
//            int powerPlantID2 = currentState.getAssignment()[clientID2];
//            int powerPlantID3 = currentState.getAssignment()[clientID3];
//            if(successorState.ringSwap(clientID1, clientID2, clientID3)){
//                double cost = h.getHeuristicValue(successorState);
//                action = "Ring Swap; Client " +  clientID1 + ": " + powerPlantID1 + " ---> " + powerPlantID2
//                            + "; Client " + clientID2 + ": " + powerPlantID2 + " ---> " + powerPlantID3
//                            + "; Client " + clientID3 + ": " + powerPlantID3 + " ---> " + powerPlantID1
//                            + "; Cost: " + cost;
//            } else{
//                double cost = h.getHeuristicValue(successorState);
//                action = "Ring swap unsuccessful. No changes; Cost: " + cost;
//            }
//        }
        successors.add(new Successor(action, successorState));
        return successors;
    }
}
