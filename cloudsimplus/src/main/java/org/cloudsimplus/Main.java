package org.cloudsimplus;

import org.cloudsimplus.core.CloudSimPlus;
public class Main {
    public static void main(String[] args) {
        FixedScenario simulationScenario = new FixedScenario();
        CloudSimPlus sim = new CloudSimPlus();
        simulationScenario.createFixedScenario2(sim);
    }
}