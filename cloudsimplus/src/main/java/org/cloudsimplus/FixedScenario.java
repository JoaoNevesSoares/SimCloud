package org.cloudsimplus;

import org.cloudsimplus.brokers.DatacenterBroker;

import java.util.ArrayList;
import java.util.List;

public class FixedScenario {

    private static final int NUM_USERS = 2;
    private static final int NUM_VMS = 1;
    private static final int HOSTS_PER_DATACENTER = 4;
    private static final int PES_PER_HOST = 4;
    private static final int HOST_MIPS = 100_000;
    private static final int VM_MIPS = 1000;
    private static final int VM_PES = 1;
    private List<Activity> workflow;
    private final ArrayList<DatacenterBroker> brokers = new ArrayList<>(NUM_USERS);

}
