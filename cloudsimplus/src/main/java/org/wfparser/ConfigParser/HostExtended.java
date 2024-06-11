package org.wfparser.ConfigParser;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.cloudsimplus.resources.PeSimple;

import java.util.List;

public class HostExtended {
    private double ram;
    private double bw;
    private double storage;
    @JsonDeserialize(contentUsing = PesSimpleDeserializer.class)
    private List<PeSimple> pes;
    private String vmScheduler;

    public double getRam() {
        return ram;
    }

    public void setRam(double ram) {
        this.ram = ram;
    }

    public double getBw() {
        return bw;
    }

    public void setBw(double bw) {
        this.bw = bw;
    }

    public double getStorage() {
        return storage;
    }

    public void setStorage(double storage) {
        this.storage = storage;
    }

    public String getVmScheduler() {
        return vmScheduler;
    }

    public void setVmScheduler(String vmScheduler) {
        this.vmScheduler = vmScheduler;
    }

    public List<PeSimple> getPes() {
        return pes;
    }

    public void setPes(List<PeSimple> pes) {
        this.pes = pes;
    }
}
