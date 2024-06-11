package org.wfparser;

import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;
import org.wfparser.tablebuilders.*;
import org.wfparser.tablebuilders.historyentries.BrokerHistoryEntry;
import org.wfparser.tablebuilders.historyentries.HostUsageHistoryEntry;
import org.wfparser.tablebuilders.historyentries.RequestsStateHistoryEntry;
import org.wfparser.tablebuilders.historyentries.ResourceUsageHistoryEntry;
import org.wfparser.workflowparser.CloudletWorkflow;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SimulationLogger {
    private final List<RequestsStateHistoryEntry> requestHistory = new ArrayList<>();
    private final List<ResourceUsageHistoryEntry> resourceUsageHistory = new ArrayList<>();
    private final List<BrokerHistoryEntry> brokerHistory = new ArrayList<>();
    private final Map<HostKey, List<HostUsageHistoryEntry>> hostUsageHistory = new ConcurrentHashMap<>();
    private final String directoryPrefix;

    public SimulationLogger(String directoryPrefix) {
        this.directoryPrefix = directoryPrefix;
    }
    public void recordHostUsage(double time, Host host) {
        long totalRequestedPes = host.getVmList().stream().mapToLong(Vm::getPesNumber).sum();
        long totalAllocatedPes = host.getVmList().stream().mapToLong(vm -> (long) Math.ceil(vm.getTotalCpuMipsUtilization() / BrokerManager.getVmSinglePeDefaultMipsCapacity())).sum();
        int totalVMsActive = host.getVmList().size();
        double totalLoad = (double) totalRequestedPes / host.getPesNumber();
        double activeLoad = host.getVmList().stream().mapToDouble(vm -> vm.getTotalCpuMipsUtilization() / host.getTotalMipsCapacity()).sum();
        double activeRequestedRatio = (totalLoad == 0 ? 0 : activeLoad / totalLoad);

        var entry = new HostUsageHistoryEntry(
                time,
                host.getDatacenter().getName(),
                "Host[" + host.getId() + "]",
                totalRequestedPes,
                totalAllocatedPes,
                totalVMsActive,
                totalLoad,
                activeLoad,
                activeRequestedRatio
        );
        HostKey key = new HostKey((int) host.getDatacenter().getId(), (int) host.getId());
        hostUsageHistory.computeIfAbsent(key, _ -> new ArrayList<>()).add(entry);
    }
    public void recordResourcesUsage(double time, List<DatacenterSimple> dcs) {
        long requestedPes = getTotalRequestedPes(dcs);
        long totalPes = getTotalPes(dcs);
        double totalMips = getTotalMips(dcs);
        double requestedMips = requestedPes * BrokerManager.getVmSinglePeDefaultMipsCapacity();
        double activeRequestedRatio = (requestedMips == 0 ? 0 : getTotalAllocatedMips(dcs) / requestedMips);
        var entry = new ResourceUsageHistoryEntry(
                time,
                requestedPes,
                (long) requestedMips,
                getTotalAllocatedMips(dcs),
                getTotalAllocatedPes(dcs),
                getTotalHostsActive(dcs),
                (double) requestedPes / totalPes,
                getTotalAllocatedMips(dcs) / totalMips,
                activeRequestedRatio
        );
        resourceUsageHistory.add(entry);
    }

    private double getTotalMips(List<DatacenterSimple> dcs) {
        return dcs.stream()
                .flatMap(dc -> dc.getHostList().stream())
                .mapToDouble(Host::getTotalMipsCapacity)
                .sum();
    }
    private long getTotalHostsActive(List<DatacenterSimple> dcs) {
        return dcs.stream()
                .flatMap(dc -> dc.getHostList().stream())
                .filter(host -> !HostSelector.getActiveVmPerHost(host).isEmpty())
                .count();
    }
    private long getTotalPes(List<DatacenterSimple> dcs) {
        return dcs.stream()
                .flatMap(dc -> dc.getHostList().stream())
                .mapToLong(Host::getPesNumber)
                .sum();
    }
    public void recordRequests(double time, List<DatacenterBrokerSimple> brokers) {
        long totalUsers = brokers.size();
        long usersActive = getCurrentUsersActive(brokers);
        long usersFinished = getFinishedUsers(brokers);
        long vmsCreated = getTotalCreatedVms(brokers);
        long vmsRunning = getCurrentVmsActive(brokers);
        long vmsFailed = getTotalFailedVms(brokers);
        var entry = new RequestsStateHistoryEntry(
                time,
                totalUsers,
                usersActive,
                usersFinished,
                vmsCreated,
                vmsRunning,
                vmsFailed
        );
        requestHistory.add(entry);
    }
    public void recordBrokerStatus(String workflowName, DatacenterBroker broker, int i) {
        String name = "broker[" + i + "]";
        long submittedTasks = broker.getCloudletSubmittedList().size();
        long completedTasks = broker.getCloudletFinishedList().size();
        long failedTasks = submittedTasks - completedTasks;
        double startTime = broker.getStartTime();

        if (failedTasks > 0) {
            var entry = new BrokerHistoryEntry(
                    name,
                    workflowName,
                    submittedTasks,
                    completedTasks,
                    failedTasks,
                    startTime,
                    99, 99, 99
            );
            brokerHistory.add(entry);
        } else {
            double finishedTime = getLastCloudletExecutionTime(broker.getCloudletFinishedList());
            double makespan = finishedTime - startTime;
            double averageTaskMakespan = getAverageTaskSpan(broker.getCloudletFinishedList());
            var entry = new BrokerHistoryEntry(
                    name,
                    workflowName,
                    submittedTasks,
                    completedTasks,
                    failedTasks,
                    startTime,
                    finishedTime,
                    makespan,
                    averageTaskMakespan
            );
            brokerHistory.add(entry);
        }
    }
    private double getLastCloudletExecutionTime(List<Cloudlet> finishedCloudlets) {
        return getLastCloudlet(finishedCloudlets).getFinishTime();
    }
    private Cloudlet getLastCloudlet(List<Cloudlet> finishedCloudlets) {
        return finishedCloudlets.stream()
                .max(Comparator.comparingDouble(Cloudlet::getFinishTime))
                .orElseThrow(NoSuchElementException::new);
    }
    private double getAverageTaskSpan(List<Cloudlet> finishedCloudlets) {
        return finishedCloudlets.stream()
                .mapToDouble(Cloudlet::getTotalExecutionTime)
                .average()
                .orElse(0);
    }
    private long getCurrentUsersActive(List<DatacenterBrokerSimple> brokers) {
        return brokers.stream().filter(DatacenterBroker::isAlive).count();
    }
    private long getCurrentVmsActive(List<DatacenterBrokerSimple> brokers) {
        return brokers.stream()
                .flatMap(broker -> broker.getVmCreatedList().stream())
                .filter(vm -> !vm.isFinished())
                .count();
    }
    private long getTotalCreatedVms(List<DatacenterBrokerSimple> brokers) {
        return brokers.stream().mapToLong(DatacenterBroker::getVmsNumber).sum();
    }
    private long getTotalFailedVms(List<DatacenterBrokerSimple> brokers) {
        return brokers.stream().mapToLong(broker -> broker.getVmFailedList().size()).sum();
    }
    private long getFinishedUsers(List<DatacenterBrokerSimple> brokers) {
        return brokers.stream().filter(broker -> !broker.isAlive()).count();
    }
    private long getTotalRequestedPes(List<DatacenterSimple> dcs) {
        return dcs.stream()
                .flatMap(dc -> dc.getHostList().stream())
                .flatMap(host -> host.getVmList().stream())
                .mapToLong(Vm::getPesNumber)
                .sum();
    }
    private long getTotalAllocatedPes(List<DatacenterSimple> dcs) {
        return dcs.stream()
                .flatMap(dc -> dc.getHostList().stream())
                .flatMap(host -> host.getVmList().stream())
                .mapToLong(vm -> (long) Math.ceil(vm.getTotalCpuMipsUtilization() / BrokerManager.getVmSinglePeDefaultMipsCapacity()))
                .sum();
    }
    private long getTotalAllocatedMips(List<DatacenterSimple> dcs) {
        return dcs.stream()
                .flatMap(dc -> dc.getHostList().stream())
                .flatMap(host -> host.getVmList().stream())
                .mapToLong(vm -> (long) vm.getTotalCpuMipsUtilization())
                .sum();
    }
    public void writeHostsTableToFile() {
        hostUsageHistory.forEach((key, historyEntries) -> {
            String filename = directoryPrefix + "host-" + key.getDatacenterId() + "-" + key.getHostId() + ".csv";
            try (PrintStream ps = new PrintStream(new FileOutputStream(filename))) {
                new HostUsageTableBuilder(historyEntries, ps).build();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
    }
    public void writeWorkflowTableToFile(List<CloudletWorkflow> workflow, String fileName) {
        try (PrintStream ps = new PrintStream(new FileOutputStream(fileName))) {
            new WorkflowTableBuilder(workflow, ps).build();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void writeDemandTableToFile() {
        try (PrintStream ps = new PrintStream(new FileOutputStream(directoryPrefix + "demand.csv"))) {
            new RequestsTableBuilder(this.requestHistory, ps).build();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void writeResourcesTableToFile() {
        try (PrintStream ps = new PrintStream(new FileOutputStream(directoryPrefix + "resources.csv"))) {
            new ResourceUsageTableBuilder(this.resourceUsageHistory, ps).build();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void writeBrokerTableToFile() {
        try (PrintStream ps = new PrintStream(new FileOutputStream(directoryPrefix + "brokers.csv"))) {
            new BrokerTableBuilder(this.brokerHistory, ps).build();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
