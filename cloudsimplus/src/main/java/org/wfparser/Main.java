package org.wfparser;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        var simManager = new SimulationManagerSingle("results/" + 1 + "-");
        simManager.setPlatformFileName("src/main/resources/low-platform.yaml");

    }
    public static void simulatebyDays() {
        int days = 1;
        List<SimulationManagerSimple> simulationList = new ArrayList<>(days);
        for(int i=1 ; i<=days ; i++) {
            simulationList.add(new SimulationManagerSimple("results/" + i + "-"));
        }
        simulationList.forEach(SimulationManagerSimple::simulate);
    }
}