package org.example;

public record ResourceUsageHistoryEntry(double time,
                                        long totalRequestedPes,
                                        long totalRequestedMips,
                                        long totalAllocatedMips,
                                        long totalAllocatedPes,
                                        double totalHostsActive
) {

    public double time() { return time;}
    public long totalRequestedPes() { return totalRequestedPes;}
    public long totalRequestedMips() { return totalRequestedMips;}
    public long totalAllocatedMips() { return totalAllocatedMips;}
    public long totalAllocatedPes() { return totalAllocatedPes;}
    public double totalHostsActive() { return totalHostsActive;}

    // TODO: TOTAL REQUESTED PES
    // TODO: TOTAL REQUESTED MIPS
    // TODO: TOTAL ALLOCATED PES
    // TODO: TOTAL ALLOCATED MIPS
    // TODO: TOTAL HOST ACTIVE
    // TODO: TOTAL USAGE MIPS
}
