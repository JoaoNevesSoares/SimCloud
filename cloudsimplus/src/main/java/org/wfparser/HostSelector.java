package org.wfparser;

import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import java.util.List;
import java.util.stream.Collectors;

public class HostSelector {
    public static Host selectHostWithMostAvailableResourcesOrNull(List<DatacenterSimple> datacenters) {
        Host hostSelected = null;
        double mostAvailableResources = 0;
        for (Datacenter datacenter : datacenters) {
            for (Host host : datacenter.getHostList()) {
                double availableResources = host.getTotalAvailableMips();
                if (availableResources > mostAvailableResources && availableResources >= BrokerManager.getVmDefaultMipsCapacity()) {
                    mostAvailableResources = availableResources;
                    hostSelected = host;
                }
            }
        }
        return hostSelected;
    }
    public static Host selectHostWithLeastVms(List<DatacenterSimple> datacenters) {
        Host hostSelected = null;
        long leastVmAmount = Long.MAX_VALUE;
        for (DatacenterSimple datacenter : datacenters) {
            for (Host host : datacenter.getHostList()) {
                long vmAmount = getActiveVmPerHost(host).size();
                if (vmAmount < leastVmAmount) {
                    leastVmAmount = vmAmount;
                    hostSelected = host;
                }
            }
        }
        return hostSelected;
    }

    public static double calculateVmMipsForBalancing(Host host, Vm vm) {
        double mipsCapacity = host.getTotalMipsCapacity();
        return mipsCapacity / ((getActiveVmPerHost(host).size() + 1) * vm.getPesNumber());
    }

    public static void updateMipsForActiveVms(Host host, double vMips) {
        getActiveVmPerHost(host).forEach(vm -> ((VmSimple) vm).getAllocatedMips().setMips(vMips));
    }

    public static Vm updateVmMips(Vm vm, double vMips) {
        VmSimple updatedVm = new VmSimple(vMips, vm.getPesNumber());
        updatedVm.setRam(vm.getRam().getCapacity());
        updatedVm.setBw(vm.getBw().getCapacity());
        updatedVm.setSize(vm.getStorage().getCapacity());
        updatedVm.setId(vm.getId());
        return updatedVm;
    }

    public static List<Vm> getActiveVmPerHost(Host host) {
        return host.getVmList()
                .stream()
                .filter(vm -> !vm.isFinished())
                .collect(Collectors.toList());
    }
}


