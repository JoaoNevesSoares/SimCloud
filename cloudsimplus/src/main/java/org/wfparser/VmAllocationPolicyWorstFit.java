package org.wfparser;

import org.cloudsimplus.allocationpolicies.VmAllocationPolicySimple;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSuitability;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VmAllocationPolicyWorstFit extends VmAllocationPolicySimple {

    @Override
    protected Optional<Host> defaultFindHostForVm(Vm vm) {
        Host hostSelected = null;
        double mostAvailableResources = 0;
        int leastActiveVms = Integer.MAX_VALUE;
        Datacenter dc = this.getDatacenter();
        for (Host host : dc.getHostList()) {
            double availableResources = host.getTotalAvailableMips();
            int activeVms = host.getVmList().size();

            if (availableResources > mostAvailableResources ||
                    (availableResources == mostAvailableResources && activeVms < leastActiveVms)) {
                mostAvailableResources = availableResources;
                leastActiveVms = activeVms;
                hostSelected = host;
            }
        }
        return Optional.ofNullable(hostSelected);
    }
    @Override
    public HostSuitability allocateHostForVm(Vm vm) {
        Optional<Host> optionalHost = this.defaultFindHostForVm(vm);
        if(optionalHost.isPresent()) {
            Host host = optionalHost.get();
            if(host.isActive() && host.getTotalAvailableMips() >= vm.getTotalMipsCapacity()) {
                return this.allocateHostForVm(vm, (Host)optionalHost.get());
            } else if (host.isActive()) {
                double vMips = calculateVmMipsForBalancing(optionalHost.get(),vm);
                vMips = Math.floor(vMips);
                System.out.println("vMips = " + vMips);
                System.out.println("host name: " + host.getId());
                System.out.println("host active vms " + host.getVmList().size());
                adjustVmMipsForHost(host,vMips);
                ((VmSimpleSetMips) vm).changeMips(vMips);
                return this.allocateHostForVm(vm, host);
            }
        }
        logNoSuitableHostFound(vm);
        return new HostSuitability(vm, "No suitable host found");
    }
    private void logNoSuitableHostFound(Vm vm) {
        LOGGER.warn("{}: {}: No suitable host found for {} in {}", vm.getSimulation().clockStr(), this.getClass().getSimpleName(), vm, getDatacenter());
    }
    private void adjustVmMipsForHost(Host host, double vmMips) {
        getActiveVmPerHost(host).forEach(vm -> ((VmSimple) vm).getAllocatedMips().setMips(vmMips));
    }
    protected double calculateVmMipsForBalancing(Host host, Vm vm) {
        double mipsCapacity = host.getTotalMipsCapacity();
        return mipsCapacity / ((getActiveVmPerHost(host).size() + 1) * vm.getPesNumber());
    }
    protected List<Vm> getActiveVmPerHost(Host host) {
        return host.getVmList()
                .stream()
                .filter(vm -> !vm.isFinished())
                .collect(Collectors.toList());
    }
}
