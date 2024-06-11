package org.wfparser.ConfigParser;

import org.cloudsimplus.resources.PeSimple;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PesExtended extends PeSimple {

    @JsonCreator
    public PesExtended(@JsonProperty("id") int id, @JsonProperty("mipsCapacity") double mipsCapacity) {
        super(mipsCapacity);
        this.setId(id);
    }
}
