package org.cloudsimplus;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class HostPOJO {
    @JacksonXmlProperty(isAttribute = true)
    private String id;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "pe")
    private List<PePOJO> cores;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<PePOJO> getCores() {
        return cores;
    }

    public void setCores(List<PePOJO> cores) {
        this.cores = cores;
    }
}
