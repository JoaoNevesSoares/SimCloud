package org.example;

import org.cloudsimplus.builders.Builder;
import org.cloudsimplus.builders.PeBuilder;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.listeners.EventListener;
import org.cloudsimplus.listeners.HostUpdatesVmsProcessingEventInfo;
import org.cloudsimplus.provisioners.ResourceProvisionerSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.schedulers.vm.VmScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class HybridHostBuilder implements Builder {
    private double mips = 1000;
    private int    pes = 12;

    private Function<List<Pe>, Host> hostCreationFunction;
    private EventListener<HostUpdatesVmsProcessingEventInfo> onUpdateVmsProcessingListener = EventListener.NULL;
    private Supplier<VmScheduler> vmSchedulerSupplier;

    public HybridHostBuilder() {
        super();
        this.hostCreationFunction = this::defaultHostCreationFunction;
    }
    public List<Host> create() {
        return create(1);
    }
    public List<Host> create(final int amount) {
        validateAmount(amount);
        final List<Host> hosts = new ArrayList<>(amount);
        for (int i = 0; i < amount; i++) {
            final List<Pe> peList = new PeBuilder().create(pes, mips);
            final Host host = hostCreationFunction.apply(peList);
            if(vmSchedulerSupplier != null) {
                host.setVmScheduler(vmSchedulerSupplier.get());
            }
            hosts.add(host);
        }
        return hosts;
    }

    private Host defaultHostCreationFunction(final List<Pe> peList) {
        return new HostSimple(peList)
                .setRamProvisioner(new ResourceProvisionerSimple())
                .setBwProvisioner(new ResourceProvisionerSimple())
                .addOnUpdateProcessingListener(onUpdateVmsProcessingListener);
    }
    public HybridHostBuilder setMips(double defaultMIPS) {
        this.mips = defaultMIPS;
        return this;
    }

    public HybridHostBuilder setPes(int defaultPEs) {
        this.pes = defaultPEs;
        return this;
    }
    public void setHostCreationFunction(final Function<List<Pe>, Host> hostCreationFunction) {
        this.hostCreationFunction = Objects.requireNonNull(hostCreationFunction);
    }
    public HybridHostBuilder setOnUpdateVmsProcessingListener(final EventListener<HostUpdatesVmsProcessingEventInfo> listener) {
        this.onUpdateVmsProcessingListener = Objects.requireNonNull(listener);
        return this;
    }
    public HybridHostBuilder setVmSchedulerSupplier(final Supplier<VmScheduler> vmSchedulerSupplier) {
        this.vmSchedulerSupplier = Objects.requireNonNull(vmSchedulerSupplier);
        return this;
    }
}
