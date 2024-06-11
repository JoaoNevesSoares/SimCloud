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

public class SimulationManagerSimple {
    protected final CloudSimPlus sim = new CloudSimPlus();

    public String getPlatformFileName() {
        return platformFileName;
    }

    public void setPlatformFileName(String platformFileName) {
        this.platformFileName = platformFileName;
    }

    protected String platformFileName;
    private List<DatacenterSimple> datacenters;
    protected SimulationLogger simLog;

    protected SimulationScheduler getSimulationScheduler() {
        return simulationScheduler;
    }
    protected List<DatacenterSimple> getDatacenters() {
        return datacenters;
    }
    private SimulationScheduler simulationScheduler;

    public BrokerManagerSimple getBrokerManager() {
        return brokerManager;
    }

    protected BrokerManagerSimple brokerManager;
    private static final int VMS_AMOUNT = 4;
    private final String directoryPrefix;

    public SimulationManagerSimple(String directoryPrefix) {
        this.directoryPrefix = directoryPrefix;
    }
    public void simulate() {
        Log.setLevel(Level.INFO);
        var platformFileName = getPlatformFileName();
        setupDatacenters(platformFileName);
        setupLogger();
        this.brokerManager = new BrokerManagerSimple();
        brokerManager.newBrokerRequest(sim, datacenters, VMS_AMOUNT);
        setupSimulationScheduler();
        sim.terminateAt(simulationScheduler.getTimeToFinish());
        sim.addOnClockTickListener(this::onClockTickFunction);
        sim.start();
        displayStatistics();
    }
    protected void setupDatacenters(String fileName) {
        PlatformUtils.init(sim);
        datacenters = PlatformUtils.loadPlatform(fileName);
        for (DatacenterSimple datacenter : datacenters) {
            datacenter.setVmAllocationPolicy(new VmAllocationPolicyWorstFit());
        }
    }
    protected void setupLogger() {
        simLog = new SimulationLogger(directoryPrefix);
    }
    protected void setupSimulationScheduler() {
        simulationScheduler = new SimulationScheduler(11,86400,60);
    }
    protected void displayStatistics() {
        displayWorkflowStatistics();
        Host host1 = datacenters.getFirst().getHost(0);
        new HostHistoryTableBuilder(host1).build();
        simLog.writeDemandTableToFile();
        simLog.writeResourcesTableToFile();
        simLog.writeBrokerTableToFile();
        simLog.writeHostsTableToFile();
    }
    protected void displayWorkflowStatistics() {
        for (int i = 0; i < brokerManager.brokers.size(); i++) {
            var broker = brokerManager.brokers.get(i);
            List<CloudletWorkflow> finishedActivities = broker.getCloudletFinishedList();
            simLog.writeWorkflowTableToFile(finishedActivities, directoryPrefix + "broker/" + i + ".md");
            simLog.recordBrokerStatus("montage", broker, i);
        }
    }
    protected void onClockTickFunction(EventInfo info) {
        double time = info.getTime();
        if (simulationScheduler.isWaitingNextEvents(time) && time <= 84500 ) {
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
    protected void logHostStatistics(double time) {
        for (DatacenterSimple datacenter : datacenters) {
            for (Host host : datacenter.getHostList()) {
                simLog.recordHostUsage(time,host);
            }
        }
    }
}
