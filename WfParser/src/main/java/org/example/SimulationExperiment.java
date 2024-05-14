package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicyRoundRobin;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.builders.BrokerBuilder;
import org.cloudsimplus.builders.BrokerBuilderDecorator;
import org.cloudsimplus.builders.SimulationScenarioBuilder;
import org.cloudsimplus.builders.VmBuilder;
import org.cloudsimplus.builders.tables.HostHistoryTableBuilder;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterCharacteristics;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.listeners.CloudletEventInfo;
import org.cloudsimplus.listeners.EventInfo;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudsimplus.schedulers.vm.VmSchedulerTimeShared;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;
import org.example.WorkflowParser.CloudletWorkflow;
import org.example.WorkflowParser.WfCommonsWorkflowDeserializerImpl;
import org.example.WorkflowParser.WorkflowInstance;

import java.io.*;
import java.util.List;

public class SimulationExperiment {
    private final String workflowName = "seismology-103";
    private final String workflowPath = "src/main/resources/" + workflowName + ".json";
    int brokerId = 0;
    int vmId = 0;
    private final CloudSimPlus simPlus;
    private final SimulationScenarioBuilder scenario;

    private final SimulationLifeCycle lifeCycle = new SimulationLifeCycle();

    private final SimulationTracking tracking = new SimulationTracking(1000,12);

    public static void main(String[] args) throws IOException {
        SimulationExperiment simulation = new SimulationExperiment();
        simulation.simulate();
    }
    public SimulationExperiment() throws FileNotFoundException {
        simPlus = new CloudSimPlus();
        scenario = new SimulationScenarioBuilder(simPlus);
    }
    @SneakyThrows
    public void simulate() {
        setupInfrastructure();
        setupInitialDemand();
        simPlus.terminateAt(lifeCycle.getTimeToFinish());
        simPlus.addOnClockTickListener(this::onClockThickFunction);
        simPlus.start();
        displayStatistics();
    }
    private void setupInfrastructure() {
        int taccHostsAmount = 3;
        int ucHostsAmount = 2;
        HybridHostBuilder hostBuilder = new HybridHostBuilder();
        hostBuilder.setHostCreationFunction(this::hostCreateFunction);
        var datacenterBuilder = scenario.getDatacenterBuilder();
        datacenterBuilder.setDatacenterCreationFunction(this::datacenterCreateFunction);
        setupDatacenter("UC", DatacenterCharacteristics.Distribution.PRIVATE,hostBuilder.create(ucHostsAmount));
        setupDatacenter("TAAC", DatacenterCharacteristics.Distribution.PRIVATE, hostBuilder.create(taccHostsAmount));
    }
    private void setupDatacenter(String name, DatacenterCharacteristics.Distribution distribution, List<Host> hosts) {
        var datacenterBuilder = scenario.getDatacenterBuilder();
        datacenterBuilder.create(hosts);
        var dc = datacenterBuilder.getDatacenters().getLast();
        dc.setName(name);
        dc.getCharacteristics().setDistribution(distribution);
    }
    private Host hostCreateFunction(List<Pe> pes) {
        final long hostRam = 64_000;
        final long hostBw = 10_000;
        final long hostStorage = 1_000_000;
        HostSimple host = new HostSimple(hostRam, hostBw, hostStorage,pes);
        host.setVmScheduler(new VmSchedulerTimeShared());
        host.enableUtilizationStats();
        host.setStateHistoryEnabled(true);
        return host;
    }
    private Datacenter datacenterCreateFunction(List<Host> hosts) {
        DatacenterSimple datacenterSimple = new DatacenterSimple(simPlus,hosts);
        int schedulingInterval = 1;
        datacenterSimple.setSchedulingInterval(schedulingInterval);
        datacenterSimple.setVmAllocationPolicy(new VmAllocationPolicyRoundRobin());
        return datacenterSimple;
    }

    private void setupInitialDemand() {
        setupVirtualMachines();
        submitWorkflowToBroker();
        lifeCycle.updateNextEventTime();
    }
    private void submitWorkflowToBroker() {
        DatacenterBroker broker = scenario.getBrokerBuilder().getBrokers().getLast();
        List<CloudletWorkflow> dag = loadWorkflow();
        submitWorkflow(broker,dag);
    }
    private List<CloudletWorkflow> loadWorkflow() {
        final var workflowDeserializer = new WfCommonsWorkflowDeserializerImpl(new ObjectMapper());
        WorkflowInstance workflow = workflowDeserializer.deserialize(workflowPath);
        return DagUtils.createDagFromWorkflow(workflow.getWorkflow());
    }
    private void setupVirtualMachines() {
        int vCPUs = 2;
        int vMips = 1000;
        int userVmAmount = 4;
        BrokerBuilder brokerBuilder = scenario.getBrokerBuilder();
        BrokerBuilderDecorator brokerBuilderDecorator = brokerBuilder.create();
        brokerBuilderDecorator.getBrokers().getLast().setName(brokerId++ + "");
        VmBuilder vmBuilder = brokerBuilderDecorator.getVmBuilder();
        configureVmBuilder(vmBuilder,vMips,vCPUs);
        vmBuilder.createAndSubmit(userVmAmount);
    }
    private void configureVmBuilder(VmBuilder vmBuilder, int mips, int pes) {
        vmBuilder.setVmCreationFunction(this::vmCreateFunction);
        vmBuilder.setMips(mips);
        vmBuilder.setPes(pes);
    }
    private Vm vmCreateFunction(final double mips, final long pes) {
        int vRam = 1000;
        int vBw = 128;
        int vSize = 1000;
        Vm vm = new VmSimple(mips,pes);
        vm.setId(vmId++);
        vm.setRam(vRam).setBw(vBw).setSize(vSize);
        vm.setCloudletScheduler(new CloudletSchedulerTimeShared());
        vm.enableUtilizationStats();
        vm.setShutDownDelay(50);
        return vm;
    }
    private void submitWorkflow(DatacenterBroker broker, List<CloudletWorkflow> dag) {
        for(CloudletWorkflow cloudletWorkflow : dag) {
            cloudletWorkflow.addOnFinishListener(this::schedule);

            if(cloudletWorkflow.isDependenciesCompleted()) {
                broker.submitCloudlet(cloudletWorkflow);
            }
        }
    }
    private void schedule(final CloudletEventInfo eventInfo) {
        CloudletWorkflow cloudletWorkflow = (CloudletWorkflow) eventInfo.getCloudlet();
        var successorTasks = cloudletWorkflow.getSuccessorTasks();
        if(successorTasks.isEmpty()) {
            var brk = cloudletWorkflow.getBroker();
            if(brk.getCloudletCreatedList().size() == brk.getCloudletFinishedList().size() + 1) {
                brk.getVmCreatedList().forEach(Vm::shutdown);
            };
            return;
        }
        // Submit each successor task whose dependencies are completed
        for(CloudletWorkflow task : successorTasks) {
            if (task.isDependenciesCompleted()) {
                cloudletWorkflow.getBroker().submitCloudlet(task);
            }
        }
    }
    private void onClockThickFunction(EventInfo info) {

        var time = info.getTime();
        if(lifeCycle.isWaitingNextEvents(time)) {
            setupVirtualMachines();
            submitWorkflowToBroker();
            lifeCycle.updateNextEventTime();
        }
        if (lifeCycle.isWaitingToRecord(time)) {
            shutdownIdleBrokers();
            tracking.recordRequests(time, scenario.getBrokerBuilder().getBrokers());
            tracking.recordResourcesUsage(time,scenario.getDatacenterBuilder().getDatacenters());
            lifeCycle.updateLastRecordTime(time);
        }
    }
    private void shutdownIdleBrokers() {
        for(DatacenterBroker broker : scenario.getBrokerBuilder().getBrokers()) {
            if( broker.getVmExecList().size() != broker.getVmCreatedList().size()) {
                broker.shutdown();
            }
        }
    }
    public void displayStatistics() throws FileNotFoundException {

        for(DatacenterBroker broker : scenario.getBrokerBuilder().getBrokers()) {
            //List<CloudletWorkflow> finishedActivities = broker.getCloudletFinishedList();
            //tracking.writeWorkflowTableToFile(finishedActivities,"output/broker-"+broker.getName()+".md");
            tracking.recordBrokerStatus(workflowName,broker);
        }

        //var host1 = scenario.getDatacenterBuilder().getDatacenters().getFirst().getHost(0);
        //new HostHistoryTableBuilder(host1).build();
        tracking.writeDemandTableToFile();
        tracking.writeResourcesTableToFile();
        tracking.writeBrokerTableToFile();
    }

}
