package org.cloudsimplus;

import org.cloudsimplus.datacenters.TimeZoned;
import org.cloudsimplus.schedulers.cloudlet.CloudletScheduler;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmAbstract;
import org.cloudsimplus.vms.VmSimple;

public class VmDynamic extends VmSimple {

    protected VmDynamic(long id, long mipsCapacity, long pesNumber) {
        super(id, mipsCapacity, pesNumber);
    }

    public void setMips(long mipsCapacity) {
        processor.setMips(mipsCapacity);
    }
    /**
     * Compare this Vm with another one based on {@link #getTotalMipsCapacity()}.
     *
     * @param obj the Vm to compare to
     * @return {@inheritDoc}
     */
    @Override
    public int compareTo(final Vm obj) {
        if(this.equals(obj)) {
            return 0;
        }

        return Double.compare(getTotalMipsCapacity(), obj.getTotalMipsCapacity()) +
                Long.compare(this.getId(), obj.getId()) +
                this.getBroker().compareTo(obj.getBroker());
    }
}
