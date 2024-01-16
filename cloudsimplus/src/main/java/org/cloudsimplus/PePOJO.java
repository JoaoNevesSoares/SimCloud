package org.cloudsimplus;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class PePOJO {
    @JacksonXmlProperty(isAttribute = true)
    private String id;
    @JacksonXmlProperty(isAttribute = true)
    private int mips;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMips() {
        return mips;
    }

    public void setMips(int mips) {
        this.mips = mips;
    }
}
