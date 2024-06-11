package org.wfparser;

import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.listeners.CloudletEventInfo;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;
import org.wfparser.ConfigParser.PlatformUtils;
import org.wfparser.workflowparser.CloudletWorkflow;

import java.util.ArrayList;
import java.util.List;

public class BrokerManager {
    private static long VM_ID = 100;
    public static final Vm vmTemplate = PlatformUtils.loadVmTemplate("src/main/resources/vm-m5xlarge.yaml");
    public List<DatacenterBrokerSimple> brokers = new ArrayList<>();

    public void newBrokerRequest(CloudSimPlus sim, List<DatacenterSimple> datacenters, long vmRequestAmount) {
        DatacenterBrokerSimple broker = new DatacenterBrokerSimple(sim);
        broker.getVmCreation().setMaxRetries(0);
        brokers.add(broker);
        for (long i = 0; i < vmRequestAmount; i++) {
            Vm vm = createVmForBroker(broker,datacenters);
            broker.submitVm(vm);
        }
        var workflow = DagUtils.loadWorkflow("src/main/resources/epigenomics-529.json");
        submitWorkflow(broker,workflow);
    }
    private static void submitWorkflow(DatacenterBroker broker, List<CloudletWorkflow> dag) {
        for(CloudletWorkflow cloudletWorkflow : dag) {
            cloudletWorkflow.addOnFinishListener(BrokerManager::schedule);
            if(cloudletWorkflow.isDependenciesCompleted()) {
                broker.submitCloudlet(cloudletWorkflow);
            }
        }
    }
    private static void schedule(final CloudletEventInfo eventInfo) {
        CloudletWorkflow cloudletWorkflow = (CloudletWorkflow) eventInfo.getCloudlet();
        var successorTasks = cloudletWorkflow.getSuccessorTasks();
        if(successorTasks.isEmpty() && isLastCloudletExecuted(cloudletWorkflow)) {
            shutdownBrokerVms(cloudletWorkflow.getBroker());
            return;
        }
        for(CloudletWorkflow task : successorTasks) {
            if (task.isDependenciesCompleted()) {
                cloudletWorkflow.getBroker().submitCloudlet(task);
            }
        }
    }
    private static void shutdownBrokerVms(DatacenterBroker broker) {
        broker.getVmCreatedList().forEach(Vm::shutdown);
    }
    private static  boolean isLastCloudletExecuted(CloudletWorkflow cloudletWorkflow) {
        var brk = cloudletWorkflow.getBroker();
        return brk.getCloudletCreatedList().size() == brk.getCloudletFinishedList().size() + 1;
    }
    private static Vm createVmForBroker(DatacenterBrokerSimple broker,List<DatacenterSimple> datacenters) {
        Vm vm = new VmSimple(vmTemplate);
        vm.setId(VM_ID++);
        Host host = HostSelector.selectHostWithMostAvailableResourcesOrNull(datacenters);
        if (host == null) {
            host = HostSelector.selectHostWithLeastVms(datacenters);
            assert host != null;
            double vMips = HostSelector.calculateVmMipsForBalancing(host, vm);
            HostSelector.updateMipsForActiveVms(host, vMips);
            vm = HostSelector.updateVmMips(vm, vMips);
        }
        host.getDatacenter().getVmAllocationPolicy().allocateHostForVm(vm, host);
        return vm;
    }
    public static double getVmDefaultMipsCapacity() {
        return vmTemplate.getTotalMipsCapacity();
    }
    public static double getVmSinglePeDefaultMipsCapacity() {
        return vmTemplate.getMips();
    }
    public void shutdownIdleBrokers() {
        for(DatacenterBroker broker : brokers) {
            if( broker.getVmExecList().size() != broker.getVmCreatedList().size()) {
                broker.shutdown();
            }
        }
    }
}

