package org.cloudsimplus;

import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterCharacteristicsSimple;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.listeners.CloudletEventInfo;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.vm.VmSchedulerTimeShared;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmCost;
import org.cloudsimplus.vms.VmSimple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class FixedScenario {

    private static final int NUM_USERS = 12;
    private static final int NUM_OF_DC = 3;
    private static final int NUM_VMS = 1;
    private static final int VM_PES = 1;
    private final ArrayList<DatacenterBroker> brokers = new ArrayList<>(NUM_USERS);
    private final HashMap<DatacenterBroker,ArrayList<VmSimple>> usersVms = new HashMap<>();

    public void createFixedScenario2(CloudSimPlus simulation) {
        List<Datacenter> datacenters = initDataCenters(simulation);
        initUserBrokers(simulation,datacenters);

        // for each user requestUserVm
        brokers.forEach(broker -> {
            requestUserVm(broker,NUM_VMS,getCustomerMipsQuota(broker));
        });
        // for each user submitWorkflow
        brokers.forEach(broker -> {
            submitWorkflow(broker,usersVms.get(broker));
        });

        // simulation runs until it completes or fails
        simulation.start();
        /* display Datacenter */
        brokers.forEach(this::displayResults);
        System.out.println("Datacenter name: " + datacenters.getFirst().getName() + "Total mips of datacenter: " + getDatacenterTotalMips(datacenters.getFirst()));
    }

    /**
     * Computes and print the cost ($) of resources (processing, bw, memory, storage)
     * for each VM inside the datacenter.
     */
    private void printTotalVmsCost(DatacenterBroker broker) {
        System.out.println();
        double totalCost = 0.0;
        int totalNonIdleVms = 0;
        double processingTotalCost = 0, memoryTotaCost = 0, storageTotalCost = 0, bwTotalCost = 0;
        for (final Vm vm : broker.getVmCreatedList()) {
            final var cost = new VmCost(vm);
            processingTotalCost += cost.getProcessingCost();
            memoryTotaCost += cost.getMemoryCost();
            storageTotalCost += cost.getStorageCost();
            bwTotalCost += cost.getBwCost();

            totalCost += cost.getTotalCost();
            totalNonIdleVms += vm.getTotalExecutionTime() > 0 ? 1 : 0;
            System.out.println(cost);
        }

        System.out.printf(
                "Total cost ($) for %3d created VMs from %3d: %8.2f$ %13.2f$ %17.2f$ %12.2f$ %15.2f$%n",
                totalNonIdleVms, broker.getVmsNumber(),
                processingTotalCost, memoryTotaCost, storageTotalCost, bwTotalCost, totalCost);
    }
    private double getCustomerMipsQuota(DatacenterBroker broker) {
        var dc = broker.getLastSelectedDc();
        int numVms = (NUM_USERS / NUM_OF_DC) * NUM_VMS;
        double totalMips = getDatacenterTotalMips(dc);
        return totalMips/numVms;
    }
    private double getHostTotalMips(Host host) {
        return host.getVmScheduler().getTotalAvailableMips();
    }
    private double getDatacenterTotalMips(Datacenter dc) {
        return dc.getHostList().stream().mapToDouble(this::getHostTotalMips).sum();
    }

    public void displayResults(DatacenterBroker userBroker) {

        new ActivityTableBuilder(userBroker.getCloudletFinishedList()).build();
        // print all cloudlets name in workflow and finished and start time
        for(Cloudlet task:  userBroker.getCloudletFinishedList()) {
            Activity activity = (Activity) task;
            System.out.println("Task: "+activity.getName()+"\tStart time: "+task.getStartTime()+"\tFinish time: "+task.getFinishTime());
        }
        printTotalVmsCost(userBroker);
    }
    private void requestUserVm(DatacenterBroker customer, int num_vms,double defaultMips) {
        ArrayList<VmSimple> vmList = new ArrayList<>(num_vms);
        for(int i = 0; i< num_vms; i++){
            vmList.add(createVm(customer,defaultMips));
        }
        usersVms.put(customer,vmList);
    }
    private VmSimple createVm(DatacenterBroker customer, double defaultMips) {
        VmSimple vm = new VmSimple(defaultMips,VM_PES);
        customer.submitVm(vm);

        /* Extra settings */

        /*

        VerticalVmScaling vmScaling = new VerticalVmScalingSimple(Processor.class, 0.1);
        vmScaling.setResourceScaling(new ResourceScalingInstantaneous());
        vm.setPeVerticalScaling(vmScaling);
        vm.addOnCreationFailureListener(evtInfo -> {
            failedVms.add(evtInfo.getVm());
            System.out.println("Failed to create VM "+evtInfo.getVm().getId());
        });
        vm.addOnHostDeallocationListener(evtInfo -> {
            Vm vmFinished = evtInfo.getVm();
            System.out.println("VM "+evtInfo.getVm().getId()+" deallocated from host "+evtInfo.getHost().getId());
            DatacenterBroker brokerFinished = vmFinished.getBroker();
            if(!failedVms.isEmpty()){
                brokerFinished.submitVm(failedVms.removeFirst());
            }
        });
        */
        return vm;
    }
    private Workflow initTasks() {
        Workflow workflow = WorkflowParser.createWorkflow("src/main/resources/epigenomics-10.json");
        assert workflow != null;
        for(Activity task: workflow.getTasks()) {
            task.addOnFinishListener(FixedScenario::schedule);
        }
        return workflow;
    }
    private void submitWorkflow(DatacenterBroker broker, ArrayList<VmSimple> vms) {
        var workflow = initTasks();
        for(Activity task: workflow.getTasks()) {
            broker.bindCloudletToVm(task,vms.getFirst());
            if(task.dependenciesCompleted())
                broker.submitCloudlet(task);
        }
    }
    private List<Datacenter> initDataCenters(CloudSimPlus simulation) {
        Platform platform = PlatformParser.loadPlatform("src/main/resources/platform.xml");
        assert platform != null;
        return platform.getDatacenters().stream()
                .map(dc -> createDatacenter(simulation,dc))
                .collect(Collectors.toList());
    }
    private Datacenter createDatacenter(CloudSimPlus sim,DatacenterPOJO dc) {
        List<Host> hosts = dc.getHosts().stream()
                .map(this::createHost)
                .collect(Collectors.toList());
        return new DatacenterSimple(sim,hosts)
                .setCharacteristics(new DatacenterCharacteristicsSimple(0.002, 0.02, 0.001, 0.005));
    }
    private Host createHost(HostPOJO host) {
        List<Pe> pes = host.getCores()
                .stream()
                .map(pe -> new PeSimple(pe.getMips()))
                .collect(Collectors.toList());
        Host h1 = new HostSimple(pes);
        h1.setVmScheduler(new VmSchedulerTimeShared());
        return h1;
    }
    private void initUserBrokers(CloudSimPlus simulation, List<Datacenter> datacenters) {
        for(int i=0; i< NUM_USERS; i++) {
            Datacenter datacenter = datacenters.get(i % datacenters.size());
            //Datacenter datacenter = datacenters.getFirst();
            DatacenterBroker broker = createDatacenterBroker(simulation,"Broker_"+i);
            broker.setLastSelectedDc(datacenter);
            this.brokers.add(broker);
        }
    }
    private DatacenterBroker createDatacenterBroker(CloudSimPlus simulation, String name) {
        var broker = new DatacenterBrokerSimple(simulation);
        broker.setName(name);
        return broker;
    }
    public static void schedule(final CloudletEventInfo eventInfo) {
        Activity activity = (Activity) eventInfo.getCloudlet();
        // check if activity is task_04
        var successors = activity.getSuccessors();
        if(successors.isEmpty()){
            return;
        }
        // get the activity corresponding to the cloudlet
        for(Activity atv : successors) {
            if (atv.dependenciesCompleted()) {
                activity.getBroker().submitCloudlet(atv);
            }
        }
    }
}
