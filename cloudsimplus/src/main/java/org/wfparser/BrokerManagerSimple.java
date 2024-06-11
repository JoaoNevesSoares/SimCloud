package org.wfparser;

import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.listeners.CloudletEventInfo;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;
import org.wfparser.ConfigParser.PlatformUtils;
import org.wfparser.workflowparser.CloudletWorkflow;

import java.util.ArrayList;
import java.util.List;

public class BrokerManagerSimple {
    private static long VM_ID = 100;
    public static final Vm vmTemplate = PlatformUtils.loadVmTemplate("src/main/resources/vm-m5xlarge.yaml");
    private String workflowFileName;
    public List<DatacenterBrokerSimple> brokers = new ArrayList<>();

    public String getWorkflowFileName() {
        return workflowFileName;
    }

    public void setWorkflowFileName(String workflowFileName) {
        this.workflowFileName = workflowFileName;
    }

    public void newBrokerRequest(CloudSimPlus sim, List<DatacenterSimple> datacenters, long vmRequestAmount) {
        DatacenterBrokerWorstFit broker = new DatacenterBrokerWorstFit(sim);
        broker.getVmCreation().setMaxRetries(0);
        brokers.add(broker);
        for (long i = 0; i < vmRequestAmount; i++) {
            VmSimpleSetMips vm = createVmForBroker(broker,datacenters);
            broker.submitVm(vm);
        }
        var workflow = DagUtils.loadWorkflow(getWorkflowFileName());
        submitWorkflow(broker,workflow);
    }
    private static VmSimpleSetMips createVmForBroker(DatacenterBrokerSimple broker,List<DatacenterSimple> datacenters) {

        VmSimpleSetMips vm = new VmSimpleSetMips(new VmSimple(vmTemplate));
        vm.setId(VM_ID++);
        return vm;
    }
    private static void submitWorkflow(DatacenterBroker broker, List<CloudletWorkflow> dag) {
        for(CloudletWorkflow cloudletWorkflow : dag) {
            cloudletWorkflow.addOnFinishListener(BrokerManagerSimple::schedule);
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
    public void shutdownIdleBrokers() {
        for(DatacenterBroker broker : brokers) {
            if( broker.getVmExecList().size() != broker.getVmCreatedList().size()) {
                broker.shutdown();
            }
        }
    }
}
