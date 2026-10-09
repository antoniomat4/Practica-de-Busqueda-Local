package ClasesPractica1;

import IA.Energia.Centrales;
import IA.Energia.Clientes;
import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;

public class P1SuccessorFunctionHC implements SuccessorFunction {

    /**
     * Creates a list of all successors for a given state
     * @param state state whose successors are to be created
     * @return a list of all successors
     */
    @Override
    public List getSuccessors(Object state) throws Exception {
        ArrayList<Successor> successors = new ArrayList<>();
        P1State currentState = (P1State) state;
        Clientes clients = currentState.getClients();
        Centrales powerPlants = currentState.getPowerPlants();
        P1HeuristicFunction h = new P1HeuristicFunction();

        //generate all successors with the changeAssignment operator
        for(int clientID=0; clientID<clients.size(); clientID++){
            int currentPowerPlantID = currentState.getAssignment()[clientID];
            for(int newPowerPlantID=-1; newPowerPlantID<powerPlants.size(); newPowerPlantID++){
                P1State successorState = new P1State(clients, powerPlants);
                successorState.setAssignment(currentState.getAssignment().clone());
                successorState.setCurrentProduction(currentState.getCurrentProduction().clone());
                //avoid doubles in the successors list
                if(successorState.getAssignment()[clientID]==newPowerPlantID
                        ||  !successorState.changeAssignment(clientID, newPowerPlantID)){
                    continue;
                }
                double cost = h.getHeuristicValue(successorState);
                String action = "Change Assignment Client " + clientID + ": power plant " +
                        currentPowerPlantID + " ---> " + newPowerPlantID + "; Cost: " + cost;
                successors.add(new Successor(action, successorState));
            }
        }
        //generate all successors with the swapAssignment operator
        for(int clientID1=0; clientID1<clients.size()-1; clientID1++){
            int powerPlantID1 = currentState.getAssignment()[clientID1];
            for(int clientID2=clientID1+1; clientID2<clients.size(); clientID2++){
                int powerPlantID2 = currentState.getAssignment()[clientID2];
                P1State successorState = new P1State(clients, powerPlants);
                successorState.setAssignment(currentState.getAssignment().clone());
                successorState.setCurrentProduction(currentState.getCurrentProduction().clone());
                if(!currentState.swapAssignment(clientID1, clientID2)){
                    continue;
                }
                double cost = h.getHeuristicValue(currentState);
                String action = "Swap Assignment Client " + clientID1 + ": " + powerPlantID1 + " ---> " + powerPlantID2
                        + "; Client " + clientID2 + ": " + powerPlantID2 + " ---> " + powerPlantID1 + "; Cost: " + cost;
                successors.add(new Successor(action, successorState));
            }
        }
        return successors;
    }
}
