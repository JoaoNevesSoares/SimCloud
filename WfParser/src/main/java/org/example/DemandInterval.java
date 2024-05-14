package org.example;

import org.cloudsimplus.distributions.ExponentialDistr;

public class DemandInterval {
    private double meanInterval;
    private ExponentialDistr exponentialDistr;
    public DemandInterval(double meanInterval) {
        this.meanInterval = meanInterval;
        this.exponentialDistr = new ExponentialDistr(meanInterval);
    }
    public double nextInterval() {
        return exponentialDistr.sample();
    }
}
