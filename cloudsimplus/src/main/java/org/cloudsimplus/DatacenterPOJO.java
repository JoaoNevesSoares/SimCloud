package org.cloudsimplus;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class DatacenterPOJO {
    @JacksonXmlProperty(isAttribute = true)
    private String id;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "host")
    private List<HostPOJO> hosts;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<HostPOJO> getHosts() {
        return hosts;
    }

    public void setHosts(List<HostPOJO> hosts) {
        this.hosts = hosts;
    }
}

