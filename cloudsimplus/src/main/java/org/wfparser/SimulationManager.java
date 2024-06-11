package org.wfparser;

import ch.qos.logback.classic.Level;
import org.cloudsimplus.builders.tables.HostHistoryTableBuilder;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.util.Log;
import org.wfparser.ConfigParser.PlatformUtils;
import org.wfparser.workflowparser.CloudletWorkflow;

import java.util.List;

public class SimulationManager {
    private final CloudSimPlus sim = new CloudSimPlus();
    private List<DatacenterSimple> datacenters;
    private SimulationLogger simLog;
    private SimulationScheduler simulationScheduler;
    private BrokerManager brokerManager;
    private static final int VMS_AMOUNT = 4;
    private final String directoryPrefix;
    public SimulationManager(String directoryPrefix) {
        this.directoryPrefix = directoryPrefix;
    }
    public void simulate() {
        Log.setLevel(Level.OFF);
        setupDatacenters();
        setupLogger();
        this.brokerManager = new BrokerManager();
        brokerManager.newBrokerRequest(sim, datacenters, VMS_AMOUNT);
        setupSimulationScheduler();
        sim.terminateAt(simulationScheduler.getTimeToFinish());
        sim.addOnClockTickListener(this::onClockTickFunction);
        sim.start();
        displayStatistics();
    }
    private void setupDatacenters() {
        PlatformUtils.init(sim);
        datacenters = PlatformUtils.loadPlatform("src/main/resources/low-platform.yaml");
    }
    private void setupLogger() {
        simLog = new SimulationLogger(directoryPrefix);
    }
    private void setupSimulationScheduler() {
        simulationScheduler = new SimulationScheduler(20,86400,3600);
    }
    private void displayStatistics() {
        displayWorkflowStatistics();
        for (int i = 0; i < brokerManager.brokers.size(); i++) {
            var broker = brokerManager.brokers.get(i);
            simLog.recordBrokerStatus("seismology", broker, i);
        }
        Host host1 = datacenters.getFirst().getHost(0);
        new HostHistoryTableBuilder(host1).build();
        simLog.writeDemandTableToFile();
        simLog.writeResourcesTableToFile();
        simLog.writeBrokerTableToFile();
        simLog.writeHostsTableToFile();
    }
    private void displayWorkflowStatistics() {
        for (int i = 0; i < brokerManager.brokers.size(); i++) {
            var broker = brokerManager.brokers.get(i);
            List<CloudletWorkflow> finishedActivities = broker.getCloudletFinishedList();
            simLog.writeWorkflowTableToFile(finishedActivities, directoryPrefix + "broker/" + i + ".md");
            simLog.recordBrokerStatus("seismology", broker, i);
        }
    }
    private void onClockTickFunction(EventInfo info) {
        double time = info.getTime();
        if (simulationScheduler.isWaitingNextEvents(time) && time <= 86000 ) {
            brokerManager.newBrokerRequest(sim, datacenters, VMS_AMOUNT);
            simulationScheduler.updateNextEventTime(time);
        }
        if (simulationScheduler.isWaitingToRecord(time)) {
            brokerManager.shutdownIdleBrokers();
            simLog.recordRequests(time, brokerManager.brokers);
            logHostStatistics(time);
            simLog.recordResourcesUsage(time, datacenters);
            simulationScheduler.updateLastRecordTime(time);
        }
    }
    private void logHostStatistics(double time) {
        for (DatacenterSimple datacenter : datacenters) {
            for (Host host : datacenter.getHostList()) {
                simLog.recordHostUsage(time,host);
            }
        }
    }
}
