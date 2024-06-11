package org.wfparser.tablebuilders.historyentries;

public record RequestsStateHistoryEntry(double time,
                                        long usersAmount,
                                        long usersActive,
                                        long usersFinishedAmount,
                                        long vmsAllocatedAmount,
                                        long vmsCurrentActive,
                                        long vmsFailedAmount) {

    public double time() {
        return time;
    }

    public long usersAmount() {return usersAmount;}
    public long usersFinishedAmount() { return usersFinishedAmount;}
    public long usersActive() { return usersActive;}
    public long vmsAllocatedAmount() {return vmsAllocatedAmount;}
    public long vmsCurrentActive(){ return vmsCurrentActive;}
    public long vmsFailedAmount() {return vmsFailedAmount;}


    @Override
    public String toString() {
        final var msg = "Time: %6.1f | Number of Users: %d | Finished Users %d | Allocated : %d VMS | FAILED : %d VMS";
        return msg.formatted(time, usersAmount,usersFinishedAmount, vmsAllocatedAmount, vmsFailedAmount);
    }
}
