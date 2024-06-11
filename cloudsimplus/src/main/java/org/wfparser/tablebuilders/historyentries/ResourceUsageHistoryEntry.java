package org.wfparser.tablebuilders.historyentries;

public record ResourceUsageHistoryEntry(double time,
                                        long totalRequestedPes,
                                        long totalRequestedMips,
                                        long totalAllocatedMips,
                                        long totalAllocatedPes,
                                        double totalHostsActive,
                                        double totalLoad,
                                        double activeLoad,
                                        double activeRequestedRatio) {

    public double time() { return time;}
    public long totalRequestedPes() { return totalRequestedPes;}
    public long totalRequestedMips() { return totalRequestedMips;}
    public long totalAllocatedMips() { return totalAllocatedMips;}
    public long totalAllocatedPes() { return totalAllocatedPes;}
    public double totalHostsActive() { return totalHostsActive;}
    public double totalLoad() { return totalLoad; }
    public double activeLoad() { return activeLoad; }
    public double activeRequestedRatio() { return activeRequestedRatio; }
}
