package org.example;

import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;
import org.example.WorkflowParser.CloudletWorkflow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class SimulationTracking {

    List<RequestsStateHistoryEntry> requestHistory = new ArrayList<>();
    List<ResourceUsageHistoryEntry> resourceUsageHistory = new ArrayList<>();
    List<BrokerHistoryEntry> brokerHistory = new ArrayList<>();
    File demandFile = new File("output/demand.csv");
    File hostsFile = new File("output/hosts.csv");
    File brokerFile = new File("output/brokers.csv");
    FileOutputStream demandOutputStream = new FileOutputStream(demandFile);
    FileOutputStream hostsOutputStream = new FileOutputStream(hostsFile);
    FileOutputStream brokerOutputStream = new FileOutputStream(brokerFile);

    private final long hostMips;
    private final int hostPes;

    public SimulationTracking(long hostMips, int hostPes) throws FileNotFoundException {
        this.hostMips = hostMips;
        this.hostPes = hostPes;
    }

    public void recordResourcesUsage(double time, List<Datacenter> dcs) {
        long requestedPes = getTotalRequestedPes(dcs);
        var entry = new ResourceUsageHistoryEntry(time,
                requestedPes,
                99,
                getTotalAllocatedMips(dcs),
                getTotalAllocatedPes(dcs),
                99.0);
        resourceUsageHistory.add(entry);
    }
    public void recordRequests(double time, List<DatacenterBroker> brokers) {
        long totalUsers = brokers.size();
        long usersActive = getCurrentUsersActive(brokers);
        long usersFinished = getFinishedUsers(brokers);
        long vmsCreated = getTotalCreatedVms(brokers);
        long vmsRunning = getCurrentVmsActive(brokers);
        long vmsFailed = getTotalFailedVms(brokers);
        var entry = new RequestsStateHistoryEntry(time,totalUsers,usersActive,usersFinished,vmsCreated,vmsRunning,vmsFailed);
        requestHistory.add(entry);
    }
    public void recordBrokerStatus(String workflowName, DatacenterBroker broker) {
        var name = broker.getName();
        long submittedTasks = broker.getCloudletSubmittedList().size();
        long completedTasks = broker.getCloudletFinishedList().size();
        long failedTasks = submittedTasks - completedTasks;
        double startTime = broker.getStartTime();

        if(failedTasks > 0 ) {
            var entry = new BrokerHistoryEntry(name,
                    workflowName,
                    submittedTasks,
                    completedTasks,
                    failedTasks,startTime,
                    99,99,
                    99);
            brokerHistory.add(entry);
        }
        else {
            double finishedTime = getLastCloudletExecutionTime(broker.getCloudletFinishedList());
            double makespan = finishedTime - startTime;
            double averageTaskMakespan = getAverageTaskSpan(broker.getCloudletFinishedList());
            var entry = new BrokerHistoryEntry(name,
                    workflowName,
                    submittedTasks,
                    completedTasks,
                    failedTasks,startTime,
                    finishedTime,makespan,
                    averageTaskMakespan);
            brokerHistory.add(entry);
        }
    }
    private double getLastCloudletExecutionTime(List<Cloudlet> finishedCloudlets) {
        return getLastCloudlet(finishedCloudlets).getFinishTime();
    }
    private Cloudlet getLastCloudlet(List<Cloudlet> finishedCloudlets){
        Cloudlet let = finishedCloudlets.getFirst();
        for (Cloudlet finishedCloudlet : finishedCloudlets) {
            if(finishedCloudlet.getFinishTime() >= let.getFinishTime()){
                let = finishedCloudlet;
            }
        }
        return let;
    }
    private double getAverageTaskSpan(List<Cloudlet> finishedCloudlets) {
        double totalExecutionTime =0;
        for (Cloudlet cloudlet : finishedCloudlets) {
            totalExecutionTime += cloudlet.getTotalExecutionTime();
        }
        return totalExecutionTime / finishedCloudlets.size();
    }
    private long getCurrentUsersActive(List<DatacenterBroker> brokers) {
        long usersActiveCount = 0;
        for(DatacenterBroker broker : brokers) {

            usersActiveCount +=  broker.isAlive() ? 1 : 0 ;
        }
        return usersActiveCount;
    }
    private long getCurrentVmsActive(List<DatacenterBroker> brokers) {
        long activeVmCount = 0;
        for (DatacenterBroker broker : brokers) {
            for (Vm vm : broker.getVmCreatedList()) {
                if(!vm.isFinished()){
                    activeVmCount++;
                }
            }
        }
        return activeVmCount;
    }
    private long getTotalCreatedVms(List<DatacenterBroker> brokers) {
        long totalCreatedVms = 0;
        for(DatacenterBroker broker: brokers) {
            totalCreatedVms += broker.getVmsNumber();
        }
        return totalCreatedVms;
    }
    private long getTotalFailedVms(List<DatacenterBroker> brokers) {
        long totalFailedVms = 0;
        for(DatacenterBroker broker: brokers) {
            totalFailedVms += broker.getVmFailedList().size();
        }
        return totalFailedVms;
    }
    private long getFinishedUsers(List<DatacenterBroker> brokers) {
        long finishedUsers = 0;
        for(DatacenterBroker broker : brokers) {

            finishedUsers +=  broker.isAlive() ? 0 : 1 ;
        }
        return finishedUsers;
    }
    private long getTotalRequestedPes(List<Datacenter> dcs) {
        long requestedPes = 0;
        for (Datacenter dc : dcs) {
            for (Host host : dc.getHostList()) {
                for (Vm vm : host.getVmList()) {
                    //requestedPes += host.getVmScheduler().getRequestedMips(vm).pes();
                    requestedPes += (long) vm.getTotalCpuMipsRequested() / hostMips;
                }
            }
        }
        return requestedPes;
    }
    private long getTotalAllocatedPes(List<Datacenter> dcs) {
        long totalAllocatedPes = 0;
        for (Datacenter dc : dcs) {
            for (Host host : dc.getHostList()) {
                for (Vm vm : host.getVmList()) {
                    totalAllocatedPes += (long) (vm.getTotalCpuMipsUtilization() / vm.getMips());
                }
            }
        }
        return totalAllocatedPes;
    }
    private long getTotalAllocatedMips(List<Datacenter> dcs) {
        long totalAllocatedMips = 0;
        for (Datacenter dc : dcs) {
            for (Host host : dc.getHostList()) {
                for (Vm vm : host.getVmList()) {
                    totalAllocatedMips += (long) vm.getTotalCpuMipsUtilization();
                }
            }
        }
        return totalAllocatedMips;
    }
    public void writeWorkflowTableToFile(List<CloudletWorkflow> workflow,String fileName) throws FileNotFoundException {
        File makespanFile = new File(fileName);
        FileOutputStream makespanOutputStream = new FileOutputStream(makespanFile);
        var ps = new PrintStream(makespanOutputStream);
        var table = new WorkflowTableBuilder(workflow,ps);
        table.build();
    }
    public void writeDemandTableToFile() {
        var ps = new PrintStream(demandOutputStream);
        var table = new RequestsTableBuilder(this.requestHistory,ps);
        table.build();
    }
    public void writeResourcesTableToFile() {
        var ps = new PrintStream(hostsOutputStream);
        var table = new ResourceUsageTableBuilder(this.resourceUsageHistory,ps);
        table.build();
    }
    public void writeBrokerTableToFile() {
        var ps = new PrintStream(brokerOutputStream);
        var table = new BrokerTableBuilder(this.brokerHistory,ps);
        table.build();
    }
}
