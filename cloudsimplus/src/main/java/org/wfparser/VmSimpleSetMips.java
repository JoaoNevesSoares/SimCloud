package org.wfparser;

import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

public class VmSimpleSetMips extends VmSimple {

    public VmSimpleSetMips(double mipsCapacity, long pesNumber) {
        super(mipsCapacity, pesNumber);
    }
    public VmSimpleSetMips(VmSimple vm ){
        super(vm);
    }
    public void changeMips(double newMips) {
        this.setMips(newMips);
    }
}
