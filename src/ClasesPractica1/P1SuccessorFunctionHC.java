package ClasesPractica1;

import IA.Energia.Centrales;
import IA.Energia.Clientes;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;

public class P1SuccessorFunctionHC implements SuccessorFunction {

    @Override
    public List getSuccessors(Object state) {
        ArrayList<State> successors = new ArrayList<>();
        State currentState = (State) state;
        Clientes clients = currentState.getClients();
        Centrales powerPlants = currentState.getPowerPlants();

        for(int clientID=0; clientID<currentState.getAssignment().length; clientID++){
            for(int powerPlantID=-1; powerPlantID<currentState.getPowerPlants().size(); powerPlantID++){
                State successor = new State(clients, powerPlants);
                successor.setAssignment(currentState.getAssignment().clone());
                //avoid doubles in the successors list
                if(successor.getAssignment()[clientID]==powerPlantID
                        ||  !successor.changeAssignment(clientID, powerPlantID)){
                    continue;
                }
                successors.add(successor);
            }
        }
        return successors;
    }
}
