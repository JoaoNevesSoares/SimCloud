package org.cloudsimplus;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class Platform {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "datacenter")
    private List<DatacenterPOJO> datacenters;
    public List<DatacenterPOJO> getDatacenters() {
        return datacenters;
    }

    public void setDatacenters(List<DatacenterPOJO> datacenters) {
        this.datacenters = datacenters;
    }
}
