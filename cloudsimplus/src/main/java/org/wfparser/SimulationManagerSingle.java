package org.wfparser;

import ch.qos.logback.classic.Level;
import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.util.Log;

public class SimulationManagerSingle extends SimulationManagerSimple {
    private static final int VMS_AMOUNT = 4;
    public SimulationManagerSingle(String directoryPrefix) {
        super(directoryPrefix);
    }
    @Override
    public void simulate() {
        Log.setLevel(Level.INFO);
        var platformFileName = getPlatformFileName();
        setupDatacenters(platformFileName);
        setupLogger();
        setupSimulationScheduler();
        this.brokerManager = new BrokerManagerSimple();
        brokerManager.newBrokerRequest(sim, getDatacenters(), VMS_AMOUNT);
        sim.addOnClockTickListener(this::onClockTickFunction);
        sim.start();
        displayStatistics();
    }
    @Override
    protected void onClockTickFunction(EventInfo info) {
        double time = info.getTime();
        if (getSimulationScheduler().isWaitingNextEvents(time)) {
            simLog.recordRequests(time, brokerManager.brokers);
            logHostStatistics(time);
            simLog.recordResourcesUsage(time, getDatacenters());
            getSimulationScheduler().updateLastRecordTime(time);
        }
    }
}
