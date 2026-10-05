package ClasesPractica1;

import IA.Energia.*;

import java.util.Arrays;

import static java.lang.Math.sqrt;


/**
 * Class for the state. A state is represented by a list the length of the number of customers.
 * At every customer's index, the index of the power plant which is responsible for them is saved.
 */
public class P1State {
    private int[] assignment;
    private static Centrales powerPlants;
    private static Clientes clients;

    public P1State(Clientes clients, Centrales powerPlants) {
        P1State.powerPlants = powerPlants;
        P1State.clients = clients;
        this.assignment = new int[clients.size()];
    }

    /**
     * Generates an initial state by only assigning clients with guaranteed contracts
     * to the next available power plant
     * @return a valid assignment of clients to power plants
     */
    public int[] generateInitialAssignment1(){
        int powerPlantID = 0;
        for (int clientID = 0; clientID < clients.size(); clientID++) {
            Cliente client = clients.get(clientID);
            if(client.getContrato() == Cliente.GARANTIZADO){
                //assign the next available power plant to clients with guaranteed contracts
                while(!changeAssignment(clientID, powerPlantID)){
                    powerPlantID++;
                    if(powerPlantID >= P1State.powerPlants.size()){
                        break;
                    }
                }//don't assign a power plant to clients with non-guaranteed contracts at all
            }else if(client.getContrato() == Cliente.NOGARANTIZADO){
                this.changeAssignment(clientID, -1);
            }
        }
        return this.assignment;
    }

    /**
     * Generates an initial state by assigning clients with guaranteed contracts to the next
     * available power plant and topping those off with clients with non-guaranteed contracts.
     * @return a valid assignment of clients to power plants
     */
    public int[] generateInitialAssignment2(){
        int powerPlantIdG = 0;
        int powerPlantIdN = 0;
        for (int clientID = 0; clientID < clients.size(); clientID++) {
            Cliente client = clients.get(clientID);
            if(client.getContrato() == Cliente.GARANTIZADO){
                //assign the next available power plant to clients with guaranteed contracts
                while(!changeAssignment(clientID, powerPlantIdG)){
                    powerPlantIdG++;
                    if(powerPlantIdG >= P1State.powerPlants.size()){
                        break;
                    }
                }
            }else if(client.getContrato() == Cliente.NOGARANTIZADO){
                //top off already visited power plants with clients with non-guaranteed contracts
                while(!changeAssignment(clientID, powerPlantIdN)){
                    powerPlantIdN++;
                    if(powerPlantIdN >= powerPlantIdG){
                        //in order to not take away space from clients with guaranteed contracts,
                        //don't assign if a new power plant would be needed
                        changeAssignment(clientID, -1);
                        break;
                    }
                }
            }
        }
        return this.assignment;
    }

    /**
     * Changes the power plant assignment of a single client. No assignment to any power plant
     * is represented by -1.
     * @param clientID Index of the client whose assignment is to be changed.
     * @param powerPlantID Index of the power plant which is to be assigned
     */
    public boolean changeAssignment(int clientID, int powerPlantID){
        //only allow no assignation if the clients contract is not guaranteed
        if(powerPlantID == -1 && clients.get(clientID).getContrato() == Cliente.NOGARANTIZADO){
            assignment[clientID] = -1;
            return true;
        }else if(powerPlantID == -1 &&  clients.get(clientID).getContrato() == Cliente.GARANTIZADO){
            return false;
        }
        Central powerPlant = powerPlants.get(powerPlantID);
        double currentProduction = this.getCurrentProduction(powerPlantID);
        double necessaryMW = getNecessaryMW(clientID, powerPlant);
        //only change the assignment if the power plant produces enough
        if(currentProduction + necessaryMW <= powerPlant.getProduccion()){
            assignment[clientID] = powerPlantID;
            return true;
        }
        return false;
    }

    /**
     * Getter for the states assignment of power plants to clients
     * @return the states assignment
     */
    public int[] getAssignment(){
        return this.assignment;
    }

    /**
     * Setter for the assignment variable
     * @param assignment Assignment which is to be set
     */
    public void setAssignment(int[] assignment) {
        this.assignment = assignment;
    }

    public Centrales getPowerPlants(){
        return powerPlants;
    }

    public Clientes getClients(){
        return clients;
    }

    /**
     * Calculates how much a power plant has to produce in order to supply a client depending on their distance.
     * @param clientID ID of the client which is to be supplied
     * @param powerPlant power plant which is to supply
     * @return necessary production in order to supply the client
     */
    private static double getNecessaryMW(int clientID, Central powerPlant) {
        Cliente client = clients.get(clientID);
        double distance = sqrt((client.getCoordX() - powerPlant.getCoordX()) * (client.getCoordX() - powerPlant.getCoordX())
                + (client.getCoordY() - powerPlant.getCoordY()) * (client.getCoordY() - powerPlant.getCoordY()));
        double lossFactor = 1 + VEnergia.getPerdida(distance);
        return client.getConsumo() * lossFactor;
    }

    /**
     * Calculates the amount of a power plant's production which is already being used by clients.
     * @param powerPlant power plant for which the used production is to be calculated
     * @return amount of the production which is already being used
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

    @Override
    public String toString() {
        return Arrays.toString(this.assignment);
    }
}
