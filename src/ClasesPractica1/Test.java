package ClasesPractica1;

import IA.Energia.Centrales;
import IA.Energia.Clientes;
import aima.search.framework.Problem;
import aima.search.framework.Search;
import aima.search.framework.SearchAgent;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class Test {
    public static void main(String[] arg) throws Exception {
        Clientes clients = new Clientes(1000, new double[]{0.25, 0.3, 0.45}, 0.75, 144);
        Centrales powerPlants = new Centrales(new int[]{5, 10, 25}, 144);
        P1State initialState = new P1State(clients, powerPlants);
        initialState.generateInitialAssignment1();
        P1HillClimbingSearch(initialState);
        P1SimulatedAnnealingSearch(initialState);
    }
    private static void P1HillClimbingSearch(P1State state)  {
        try {
            System.out.println("\nP1 Hill-Climbing:\n");
            Problem problem = new Problem(state, new P1SuccessorFunctionHC(), new P1GoalTest(), new P1HeuristicFunction());
            Search search = new HillClimbingSearch();
            SearchAgent agent = new SearchAgent(problem, search);

            printActions(agent.getActions());
            printInstrumentation(agent.getInstrumentation());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void P1SimulatedAnnealingSearch(P1State state) {
        System.out.println("\nP1 Simulated Annealing:\n");
        try {
            Problem problem =  new Problem(state,new P1SuccessorFunctionSA(), new P1GoalTest(),new P1HeuristicFunction());
            SimulatedAnnealingSearch search =  new SimulatedAnnealingSearch(2000,100,5,0.001);
            SearchAgent agent = new SearchAgent(problem,search);

            System.out.println();
            printActions(agent.getActions());
            printInstrumentation(agent.getInstrumentation());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printInstrumentation(Properties properties) {
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String property = properties.getProperty(key);
            System.out.println(key + " : " + property);
        }
    }

    private static void printActions(List actions) {
        for (int i = 0; i < actions.size(); i++) {
            String action = (String) actions.get(i);
            System.out.println(action);
        }
    }
}
