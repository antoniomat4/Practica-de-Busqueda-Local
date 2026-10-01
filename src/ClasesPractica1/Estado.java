package ClasesPractica1;

import IA.Energia.Centrales;
import IA.Energia.Central;
import IA.Energia.Clientes;
import IA.Energia.Cliente;


/**
 * Class for the state. A state is represented by a list the length of the number of customers.
 * At every customer's index, the index of the power plant which is responsible for them is saved.
 */
public class Estado {
    private int[] assignment;
    private static Centrales powerPlants;
    private static Clientes clients;


    public Estado(Clientes clients, Centrales powerPlants) {
        Estado.powerPlants = powerPlants;
        Estado.clients = clients;
        this.assignment = new int[clients.size()];

        //initialize initial state by only assigning clients with guaranteed contracts
        // to the next available power plant
        int powerPlantID = 0;
        for (int clientID = 0; clientID < this.assignment.length; clientID++) {
            Cliente client = clients.get(clientID);
            if(client.getContrato() == Cliente.GARANTIZADO){
                while(!changeAssignment(clientID, powerPlantID)){
                    powerPlantID++;
                    if(powerPlantID > Estado.powerPlants.size()){
                        break;
                    }
                }
            }else if(client.getContrato() == Cliente.NOGARANTIZADO){
                this.changeAssignment(clientID, -1);
            }
        }
    }

    /**
     * Changes the power plant assignment of a client. No assignment to any power plant
     * is represented by -1.
     * @param clientID Index of the client whose assignment is to be changed.
     * @param powerPlantID Index of the power plant which is to be assigned
     */
    public boolean changeAssignment(int clientID, int powerPlantID){
        if(powerPlantID == -1){
            assignment[clientID] = -1;
            return true;
        }
        Central powerPlant = powerPlants.get(powerPlantID);
        Cliente client = clients.get(clientID);
        //TODO zusätzlich nötige Produktion wegen Distanz prüfen
        if(this.getCurrentProduction(powerPlantID) + client.getConsumo()
                <= powerPlant.getProduccion()){
        assignment[clientID] = powerPlantID;
        return true;
        }
        return false;
    }

    /**
     * Calculates the amount of a power plant's production which is already being used by clients.
     * @param powerPlant power plant for which the used production is to be calculated
     * @return amount of production which is already being used
     */
    private double getCurrentProduction(int powerPlant){
        double production = 0;
        for(int i = 0; i<this.assignment.length; i++){
            if(this.assignment[i] == powerPlant){
                production += clients.get(i).getConsumo();
            }
        }
        return production;
    }
}
