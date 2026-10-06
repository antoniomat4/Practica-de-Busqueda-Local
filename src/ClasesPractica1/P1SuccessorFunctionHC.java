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

        for(int clientID=0; clientID<currentState.getAssignment().length; clientID++){
            int currentPowerPlantID = currentState.getAssignment()[clientID];
            for(int newPowerPlantID=-1; newPowerPlantID<currentState.getPowerPlants().size(); newPowerPlantID++){
                P1State successorState = new P1State(clients, powerPlants);
                successorState.setAssignment(currentState.getAssignment().clone());
                //avoid doubles in the successors list
                if(successorState.getAssignment()[clientID]==newPowerPlantID
                        ||  !successorState.changeAssignment(clientID, newPowerPlantID)){
                    continue;
                }
                double cost = h.getHeuristicValue(successorState);
                String action = "Change Assignment Client " + clientID + ": power plant " +
                        currentPowerPlantID + " ---> " + newPowerPlantID + " Cost: " + cost;
                successors.add(new Successor(action, successorState));
            }
        }
        return successors;
    }
}
