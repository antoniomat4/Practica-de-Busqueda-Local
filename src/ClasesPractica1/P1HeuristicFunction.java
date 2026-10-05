package ClasesPractica1;

import IA.Energia.Central;
import IA.Energia.Cliente;
import IA.Energia.VEnergia;
import aima.search.framework.HeuristicFunction;

import java.util.ArrayList;

public class P1HeuristicFunction implements HeuristicFunction {

    @Override
    public double getHeuristicValue(Object state) throws Exception {
        P1State currentState = (P1State) state;
        int[] assignment = currentState.getAssignment();
        ArrayList<Cliente> clients = currentState.getClients();
        ArrayList<Central> powerPlants = currentState.getPowerPlants();
        double cost = 0;
        int[] seenPowerPlants = new int[powerPlants.size()];

        for(int clientID = 0; clientID < assignment.length; clientID++){
            Cliente client = clients.get(clientID);
            int powerPlantID = assignment[clientID];

            if(powerPlantID == -1){
            //add penalty for not supplying a customer to the cost
                cost += VEnergia.getTarifaClientePenalizacion(client.getTipo());
            }else{
                Central powerPlant = powerPlants.get(powerPlantID);
                //subtract client's payment
                double rate;
                if(client.getContrato() == Cliente.GARANTIZADO){
                    rate = VEnergia.getTarifaClienteGarantizada(client.getTipo());
                }else {
                    rate = VEnergia.getTarifaClienteNoGarantizada(client.getTipo());
                }
                cost -= client.getConsumo() * rate;

                //add production cost
                if(seenPowerPlants[powerPlantID] == 0){
                    cost += powerPlant.getProduccion() * VEnergia.getCosteProduccionMW(powerPlant.getTipo()) + VEnergia.getCosteMarcha(powerPlant.getTipo());
                    seenPowerPlants[powerPlantID] = 1;
                }
            }
        }
        //add cost of idleness
        for(int powerPlantID = 0; powerPlantID < seenPowerPlants.length; powerPlantID++){
            if(seenPowerPlants[powerPlantID] == 0){
                cost += VEnergia.getCosteParada(powerPlants.get(powerPlantID).getTipo());
            }
        }
        return cost;
    }
}
