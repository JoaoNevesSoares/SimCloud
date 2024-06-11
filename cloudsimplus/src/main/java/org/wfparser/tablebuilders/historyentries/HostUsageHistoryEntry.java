package org.wfparser.tablebuilders.historyentries;

public record HostUsageHistoryEntry(double time,
                                    String dcName,
                                    String hostName,
                                    long totalRequestedPes,
                                    long totalAllocatedPes,
                                    int totalVMsActive,
                                    double totalLoad,
                                    double activeLoad,
                                    double activeRequestedRatio) {

    public double time() { return time;}
    public String dcName() { return dcName;}
    public String hostName() { return hostName;}
    public long totalRequestedPes() { return totalRequestedPes;}
    public long totalAllocatedPes() { return totalAllocatedPes;}
    public int totalVMsActive() { return totalVMsActive;}
    public double totalLoad() { return totalLoad; }
    public double activeLoad() { return activeLoad; }
    public double activeRequestedRatio() { return activeRequestedRatio; }
}

