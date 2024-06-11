package org.wfparser;

import java.util.Objects;

public class HostKey {
    private final int datacenterId;
    private final int hostId;

    public HostKey(int datacenterId, int hostId) {
        this.datacenterId = datacenterId;
        this.hostId = hostId;
    }

    public int getDatacenterId() { return datacenterId; }
    public int getHostId() { return hostId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HostKey hostKey = (HostKey) o;
        return datacenterId == hostKey.datacenterId && hostId == hostKey.hostId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(datacenterId, hostId);
    }
}

