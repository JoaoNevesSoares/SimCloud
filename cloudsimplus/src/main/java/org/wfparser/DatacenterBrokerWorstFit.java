package org.wfparser;

import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

import java.util.Comparator;
import java.util.HashMap;

public class DatacenterBrokerWorstFit extends DatacenterBrokerSimple {

    HashMap<Datacenter, Integer> datacenterVmAmountMap = new HashMap<>();
    public DatacenterBrokerWorstFit(CloudSimPlus simulation) {
        super(simulation);
        calculateVmAmountMap();
    }
    private void calculateVmAmountMap() {
        for (Datacenter datacenter : getDatacenterList()) {
            int vmAmount = 0;
            for (Host host : datacenter.getHostList()) {
                vmAmount += host.getVmCreatedList().size();
            }
            datacenterVmAmountMap.put(datacenter, vmAmount);
        }
    }
    /*
     *
     * Selects the datacenter with least vm created
     */
    @Override
    protected Datacenter defaultDatacenterMapper(final Datacenter lastDatacenter, final Vm vm) {
        Datacenter selectedDatacenter = getDatacenterList().stream()
                .min(Comparator.comparingInt(datacenter -> datacenterVmAmountMap.getOrDefault(datacenter, 0)))
                .orElse(Datacenter.NULL);

        if (selectedDatacenter != Datacenter.NULL) {
            datacenterVmAmountMap.put(selectedDatacenter, datacenterVmAmountMap.getOrDefault(selectedDatacenter, 0) + 1);
        }

        return selectedDatacenter;
    }
}
