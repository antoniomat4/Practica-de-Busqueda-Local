package ClasesPractica1;

import IA.Energia.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static IA.Energia.Cliente.GARANTIZADO;
import static IA.Energia.Cliente.NOGARANTIZADO;
import static java.lang.Math.sqrt;
import static java.util.Collections.shuffle;


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
     * Generates an initial assignment by assigning customers with larger demand first.
     */
    public void generateInitialAssignmentLargeFirst(){
        ArrayList<Integer> guaranteedClients = getClientsWithType(GARANTIZADO);
        ArrayList<Integer> notGuaranteedClients = getClientsWithType(NOGARANTIZADO);

        //sort the clients by demand in descending order
        Comparator<Integer> demandComparator = (id1, id2) -> {
            double demand1 = clients.get(id1).getConsumo();
            double demand2 = clients.get(id2).getConsumo();
            return Double.compare(demand1, demand2);
        };

        guaranteedClients.sort(demandComparator);
        notGuaranteedClients.sort(demandComparator);

        assignClientsGreedy(guaranteedClients, GARANTIZADO);
        assignClientsGreedy(notGuaranteedClients, NOGARANTIZADO);
    }


    /**
     * Calls assignClients to generate an initial assignment of clients to power plants by picking the next available
     * one closest to the client
     */
    public void generateInitialAssignmentGreedy(){
        ArrayList<Integer> guaranteedClients = getClientsWithType(GARANTIZADO);
        ArrayList<Integer> notGuaranteedClients = getClientsWithType(NOGARANTIZADO);

        assignClientsGreedy(guaranteedClients, GARANTIZADO);
        assignClientsGreedy(notGuaranteedClients, NOGARANTIZADO);
    }

    public void generateInitialAssignmentRandom(){
        ArrayList<Integer> guaranteedClients = getClientsWithType(GARANTIZADO);
        int tries = 1;
        while(true){
            //reset assignment for every try
            int[] assignmentReset = new int[clients.size()];
            Arrays.fill(assignmentReset, -1);
            setAssignment(assignmentReset);
            boolean allAssigned = true;

            for(int clientID : guaranteedClients){
                //generate random order of power plants to try for every client
                ArrayList<Integer> powerPlantIDs = new ArrayList<>();
                for(int i = 0; i < powerPlants.size(); i++ ){
                    powerPlantIDs.add(i);
                }
                shuffle(powerPlantIDs);
                boolean clientAssigned = false;
                for(int powerPlantID : powerPlantIDs){
                    if(changeAssignment(clientID, powerPlantID)){
                        clientAssigned = true;
                        break;
                    }
                }
                // start over if a client could not be assigned
                if(!clientAssigned) {
                    allAssigned = false;
                    break;
                }
            }
            if(allAssigned){
                System.out.println("Tries to create initial assignment: " + tries);
                return;
            }
            tries++;
        }
    }

    /**
     * Assigns clients to the next available power plant closest to them
     * @param clientType Type of clients to be assigned
     */
    private void assignClientsGreedy(ArrayList<Integer> clientIDs, int clientType){
        for(int clientID : clientIDs){
            Cliente client = clients.get(clientID);
            int bestPowerPlantID = getClosestFreePowerPlant(clientID);

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

        if(newPowerPlantID == oldPowerPlantID) return false;

        if(newPowerPlantID == -1){
            if(clients.get(clientID).getContrato() == GARANTIZADO) return false;
            this.currentProduction[oldPowerPlantID] -= getNecessaryMW(clientID, powerPlants.get(oldPowerPlantID));
            this.assignment[clientID] = -1;
            return true;
        }

        Central powerPlant = powerPlants.get(newPowerPlantID);
        double currentProduction = this.getCurrentProduction()[newPowerPlantID];
        double necessaryMW = getNecessaryMW(clientID, powerPlant);
        //check whether the new power plant has enough capacity to supply the client
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
        if(powerPlantID1 == powerPlantID2
                || powerPlantID1 == -1 && clients.get(clientID2).getContrato() == GARANTIZADO
                || powerPlantID2 == -1 && clients.get(clientID1).getContrato() == GARANTIZADO) return false;

        boolean swap1ok = isSwapPossible(powerPlantID1, clientID1, clientID2);
        boolean swap2ok = isSwapPossible(powerPlantID2, clientID2, clientID1);

        if(swap1ok && swap2ok){
            this.assignment[clientID1] = powerPlantID2;
            this.assignment[clientID2] = powerPlantID1;
            updateProductionAfterSwap(powerPlantID1, clientID1, clientID2);
            updateProductionAfterSwap(powerPlantID2, clientID2, clientID1);
            return true;
        }
        return false;
    }

    /**
     * Swaps the assignment of three clients in a circle: 1->2, 2->3, 3->1
     * @param clientID1 ID of the first client involved in the swap
     * @param clientID2 ID of the second client involved in the swap
     * @param clientID3 ID of the third client involved in the swap
     * @return a boolean indicating whether the swap was successful
     */
    public boolean ringSwap(int clientID1, int clientID2, int clientID3){
        int powerPlantID1 = this.assignment[clientID1];
        int powerPlantID2 = this.assignment[clientID2];
        int powerPlantID3 = this.assignment[clientID3];

        //don't swap if two clients are assigned to the same power plant
        // or a client with a guaranteed contract would not be assigned
        if(powerPlantID1 == powerPlantID2 || powerPlantID1 == powerPlantID3 || powerPlantID2 == powerPlantID3
                || powerPlantID1 == -1 && clients.get(clientID3).getContrato() == GARANTIZADO
                || powerPlantID2 == -1 && clients.get(clientID1).getContrato() == GARANTIZADO
                || powerPlantID3 == -1 && clients.get(clientID2).getContrato() == GARANTIZADO) return false;

        boolean swap1ok = isSwapPossible(powerPlantID1, clientID1, clientID3);
        boolean swap2ok = isSwapPossible(powerPlantID2, clientID2, clientID1);
        boolean swap3ok = isSwapPossible(powerPlantID3, clientID3, clientID2);

        if(swap1ok && swap2ok && swap3ok){
            this.assignment[clientID1] = powerPlantID2;
            this.assignment[clientID2] = powerPlantID3;
            this.assignment[clientID3] = powerPlantID1;
            updateProductionAfterSwap(powerPlantID1, clientID1, clientID3);
            updateProductionAfterSwap(powerPlantID2, clientID2, clientID3);
            updateProductionAfterSwap(powerPlantID3, clientID3, clientID1);
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

    /**
     * Updates the production array after a swap.
     * @param powerPlantID ID of the power plant whose production is to be updated.
     * @param oldClientID ID of the old client who leaves the power plant.
     * @param newClientID ID of the new client who is assigned to the power plant.
     */
    private void updateProductionAfterSwap(int powerPlantID, int oldClientID, int newClientID){
        if(powerPlantID != -1){
            this.currentProduction[powerPlantID] += getNecessaryMW(newClientID, powerPlants.get(powerPlantID))
                    - getNecessaryMW(oldClientID, powerPlants.get(powerPlantID));
        }
    }

    public void setCurrentProduction(double[] currentProduction){
        this.currentProduction = currentProduction;
    }

    private static double getDistance(Cliente client, Central powerPlant){
        return sqrt((client.getCoordX() - powerPlant.getCoordX()) * (client.getCoordX() - powerPlant.getCoordX())
                + (client.getCoordY() - powerPlant.getCoordY()) * (client.getCoordY() - powerPlant.getCoordY()));
    }

    /**
     * Returns a list of IDs whose corresponding clients have a given contract type
     * @param contractType contract type which the returned clients should have (GARANTIZADO or NOGARANTIZADO)
     * @return a list of client IDs of a specific contract type
     */
    private ArrayList<Integer> getClientsWithType(int contractType){
        ArrayList<Integer> clientsWithType = new ArrayList<>();
        for(int clientID = 0; clientID < clients.size(); clientID++){
            if(clients.get(clientID).getContrato() == contractType) clientsWithType.add(clientID);
        }
        return clientsWithType;
    }

    /**
     * Returns the ID of the power plant which is closest to a client and can supply them
     * @param clientID ID of the client whose closest free power plant is to be returned.
     * @return ID of the power plant which is closest to the given client and can supply them.
     */
    private int getClosestFreePowerPlant(int clientID){
        Cliente client =  clients.get(clientID);
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
        return bestPowerPlantID;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.assignment);
    }
}
