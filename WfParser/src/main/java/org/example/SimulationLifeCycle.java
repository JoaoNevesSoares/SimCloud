package org.example;

public class SimulationLifeCycle {
    private final double meanIntervalBetweenUsers = 20; // in minutes
    private final double totalSimulationTime = 86_410; // one day
    private final double sampleInterval = 3600; // 1 hour
    private double lastRecordTime = 0;
    private double nextEventTime = 0;
    private final DemandInterval ExponentialDistribution = new DemandInterval(meanIntervalBetweenUsers * 60);

    public void updateNextEventTime() {
        nextEventTime += (int) ExponentialDistribution.nextInterval();
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
    public double getTimeToFinish(){
        return totalSimulationTime;
    }
}
