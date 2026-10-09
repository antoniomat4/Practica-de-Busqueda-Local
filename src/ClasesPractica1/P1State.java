package ClasesPractica1;

import IA.Energia.*;

import java.util.Arrays;

import static IA.Energia.Cliente.GARANTIZADO;
import static IA.Energia.Cliente.NOGARANTIZADO;
import static java.lang.Math.sqrt;


/**
 * Class for the state. A state is represented by a list the length of the number of customers.
 * At every customer's index, the index of the power plant which is responsible for them is saved.
 */
public class P1State {
    private int[] assignment;
    private double[] currentProduction;
    private static Centrales powerPlants;
    private static Clientes clients;

    public P1State(Clientes clients, Centrales powerPlants) {
        P1State.powerPlants = powerPlants;
        P1State.clients = clients;
        this.assignment = new int[clients.size()];
        this.currentProduction = new double[powerPlants.size()];
        Arrays.fill(this.assignment, -1);
    }

    /**
     * Generates an initial state by first assigning clients with guaranteed contracts to the next
     * available power plant and then topping them off with clients with non-guaranteed contracts.
     */
    public void generateInitialAssignment1(){
        int powerPlantID = 0;
        for (int clientID = 0; clientID < clients.size(); clientID++) {
            Cliente client = clients.get(clientID);
            if(client.getContrato() == GARANTIZADO){
                //assign the next available power plant to clients with guaranteed contracts
                while(!changeAssignment(clientID, powerPlantID)){
                    if(powerPlantID + 1 >= P1State.powerPlants.size()){
                        System.out.println("Not enough power plants for all clients with guaranteed contracts.");
                        break;
                    }
                    powerPlantID++;
                }
            }
        }
        for (int clientID = 0; clientID < clients.size(); clientID++) {
            Cliente client = clients.get(clientID);
            if(client.getContrato() == Cliente.NOGARANTIZADO){
                //assign the next available power plant to clients with guaranteed contracts
                while(!changeAssignment(clientID, powerPlantID)){
                    if(powerPlantID >= P1State.powerPlants.size()-1){
                        changeAssignment(clientID, -1);
                        break;
                    }
                    powerPlantID++;
                }
            }
        }
    }

    /**
     * Calls assignClients to generate an initial assignment of clients to power plants by picking the next available
     * one closest to the client
     */
    public void generateInitialAssignment2(){
        assignClients(GARANTIZADO);
        assignClients(NOGARANTIZADO);
    }

    /**
     * Assigns clients to the next available power plant closest to them
     * @param clientType Type of clients to be assigned
     */
    private void assignClients(int clientType){
        for(int clientID = 0; clientID < clients.size(); clientID++){
            Cliente client = clients.get(clientID);
            if(client.getContrato() != clientType) continue;

            int bestPowerPlantID = -1;
            double bestDistance =  Double.MAX_VALUE;
            for(int powerPlantID =0;  powerPlantID < powerPlants.size(); powerPlantID++){
                Central powerPlant = powerPlants.get(powerPlantID);
                if(getNecessaryMW(clientID, powerPlant) + currentProduction[powerPlantID] <= powerPlant.getProduccion()){
                    double distance = getDistance(client, powerPlant);
                    if(distance < bestDistance){
                        bestPowerPlantID = powerPlantID;
                        bestDistance = distance;
                    }
                }
            }
            if(bestPowerPlantID != -1){
                changeAssignment(clientID, bestPowerPlantID);
            }else if(clientType == GARANTIZADO){
                System.out.println("Client " + clientID + " with a guaranteed contract could not be assigned.");
            }else{
                changeAssignment(clientID, -1);
            }
        }
    }

    /**
     * Changes the power plant assignment of a single client. No assignment to any power plant
     * is represented by -1.
     * @param clientID Index of the client whose assignment is to be changed.
     * @param newPowerPlantID Index of the power plant which is to be assigned
     * @return a boolean which is true if the swap has been successful
     */
    public boolean changeAssignment(int clientID, int newPowerPlantID){
        int oldPowerPlantID = this.assignment[clientID];

        if((newPowerPlantID == -1 &&  clients.get(clientID).getContrato() == GARANTIZADO)
                || newPowerPlantID == assignment[clientID]){
            return false;
        //only allow no assignation if the clients contract is not guaranteed
        } else if(newPowerPlantID == -1 && clients.get(clientID).getContrato() == Cliente.NOGARANTIZADO){
            this.currentProduction[oldPowerPlantID] -= getNecessaryMW(clientID, powerPlants.get(oldPowerPlantID));
            this.assignment[clientID] = -1;
            return true;
        }

        Central powerPlant = powerPlants.get(newPowerPlantID);
        double currentProduction = this.getCurrentProduction()[newPowerPlantID];
        double necessaryMW = getNecessaryMW(clientID, powerPlant);
        //only change the assignment if the power plant produces enough
        if(currentProduction + necessaryMW <= powerPlant.getProduccion()){
            if(oldPowerPlantID != -1){  //only update the production array if it concerns a real power plant
                this.currentProduction[oldPowerPlantID] -= getNecessaryMW(clientID, powerPlants.get(oldPowerPlantID));
            }
            assignment[clientID] = newPowerPlantID;
            this.currentProduction[newPowerPlantID] += getNecessaryMW(clientID, powerPlants.get(newPowerPlantID));
            return true;
        }
        return false;
    }

    /**
     * Operator to swap the assignment of two clients
     * @param clientID1 first client to be swapped
     * @param clientID2 second client so be swapped
     * @return a boolean which is true if the swap has been successful
     */
    public boolean swapAssignment(int clientID1, int clientID2){
        int powerPlantID1 = this.assignment[clientID1];
        int powerPlantID2 = this.assignment[clientID2];

        //don't swap if both clients are already assigned to the same power plant
        //or a client with a guaranteed contract would not be assigned
        if(powerPlantID1 == powerPlantID2) return false;
        if(powerPlantID1 == -1 && clients.get(clientID2).getContrato() == GARANTIZADO) return false;
        if(powerPlantID2 == -1 && clients.get(clientID1).getContrato() == GARANTIZADO) return false;

        boolean swap1ok = isSwapPossible(powerPlantID1, clientID1, clientID2);
        boolean swap2ok = isSwapPossible(powerPlantID2, clientID2, clientID1);

        if(swap1ok && swap2ok){
            this.assignment[clientID1] = powerPlantID2;
            this.assignment[clientID2] = powerPlantID1;
            if(powerPlantID1 != -1){
            this.currentProduction[powerPlantID1] +=  getNecessaryMW(clientID2, powerPlants.get(powerPlantID1))
                    - getNecessaryMW(clientID1, powerPlants.get(powerPlantID1));
            }
            if(powerPlantID2 != -1){
            this.currentProduction[powerPlantID2] += getNecessaryMW(clientID1, powerPlants.get(powerPlantID2))
                    - getNecessaryMW(clientID2, powerPlants.get(powerPlantID2));
            }
            return true;
        }
        return false;
    }

    /**
     * Helper function for the swap operator, deciding whether a swap is possible
     * depending on the capacity of the power plant and the demand to supply the new client
     * @param powerPlantID power plant whose capacity is to be checked
     * @param oldClientID old client who would be assigned to a different power plant
     * @param newClientID new client which would be assigned to the power plant
     * @return a boolean which is true if the swap is possible
     */
    private boolean isSwapPossible(int powerPlantID, int oldClientID, int newClientID){
        if(powerPlantID == -1){
            return true;
        }
        double currentProduction = this.getCurrentProduction()[powerPlantID];
        double addedMW = getNecessaryMW(newClientID, powerPlants.get(powerPlantID));
        double removedMW = getNecessaryMW(oldClientID, powerPlants.get(powerPlantID));

        return (currentProduction + addedMW - removedMW <= powerPlants.get(powerPlantID).getProduccion());
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
        double distance = getDistance(client, powerPlant);
        double lossFactor = 1 + VEnergia.getPerdida(distance);
        return client.getConsumo() * lossFactor;
    }

    public double[] getCurrentProduction(){
        return this.currentProduction;
    }

    public void setCurrentProduction(double[] currentProduction){
        this.currentProduction = currentProduction;
    }

    private static double getDistance(Cliente client, Central powerPlant){
        return sqrt((client.getCoordX() - powerPlant.getCoordX()) * (client.getCoordX() - powerPlant.getCoordX())
                + (client.getCoordY() - powerPlant.getCoordY()) * (client.getCoordY() - powerPlant.getCoordY()));
    }

    @Override
    public String toString() {
        return Arrays.toString(this.assignment);
    }
}
