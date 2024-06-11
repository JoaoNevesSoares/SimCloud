package org.wfparser.ConfigParser;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.cloudsimplus.hosts.HostSimple;

import java.util.List;

public class DatacenterExtended {
    private String id;
    @JsonDeserialize(contentUsing = HostDeserializer.class)
    private List<HostSimple> hosts;
    private DatacenterCharacteristicsExtended datacenterCharacteristics;
    private String vmAllocationPolicy;
    private double schedulingInterval;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVmAllocationPolicy() {
        return vmAllocationPolicy;
    }

    public void setVmAllocationPolicy(String vmAllocationPolicy) {
        this.vmAllocationPolicy = vmAllocationPolicy;
    }

    public double getSchedulingInterval() {
        return schedulingInterval;
    }

    public void setSchedulingInterval(double schedulingInterval) {
        this.schedulingInterval = schedulingInterval;
    }

    public List<HostSimple> getHosts() {
        return hosts;
    }

    public void setHosts(List<HostSimple> hosts) {
        this.hosts = hosts;
    }

    public DatacenterCharacteristicsExtended getDatacenterCharacteristics() {
        return datacenterCharacteristics;
    }

    public void setDatacenterCharacteristics(DatacenterCharacteristicsExtended datacenterCharacteristics) {
        this.datacenterCharacteristics = datacenterCharacteristics;
    }
}
