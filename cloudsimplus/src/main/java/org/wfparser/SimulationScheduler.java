package org.wfparser;

public class SimulationScheduler {
    private final long  meanRequestInterval; // in minutes
    private final long totalSimulationTime; // one day
    private final long sampleInterval; // 1 hour
    private double lastRecordTime = 0;
    private double nextEventTime = 0;
    private final DemandInterval exponentialDistribution;

    public SimulationScheduler(long meanRequestInterval, long totalSimulationTime, long sampleInterval) {
        this.meanRequestInterval = meanRequestInterval * 60; // converts to seconds
        this.totalSimulationTime = totalSimulationTime;
        this.sampleInterval = sampleInterval;
        exponentialDistribution = new DemandInterval(this.meanRequestInterval);
    }
    public void updateNextEventTime(double currentEventTime) {
        nextEventTime += (int) exponentialDistribution.nextInterval(currentEventTime);
    }
    public boolean isWaitingNextEvents(double time) {
        return time >= nextEventTime;
    }
    public boolean isWaitingToRecord(double time) {
        return time >= lastRecordTime + sampleInterval;
    }
    public void updateLastRecordTime(double time) {
        lastRecordTime = time;
    }
    public long getTimeToFinish(){
        return totalSimulationTime;
    }
}