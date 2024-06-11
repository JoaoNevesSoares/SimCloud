package org.wfparser.ConfigParser;

public class DatacenterCharacteristicsExtended {
    private double cpuCost;
    private double ramCost;
    private double storageCost;
    private String distribution;

    public double getCpuCost() {
        return cpuCost;
    }

    public void setCpuCost(double cpuCost) {
        this.cpuCost = cpuCost;
    }

    public double getRamCost() {
        return ramCost;
    }

    public void setRamCost(double ramCost) {
        this.ramCost = ramCost;
    }

    public double getStorageCost() {
        return storageCost;
    }

    public void setStorageCost(double storageCost) {
        this.storageCost = storageCost;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }
}
