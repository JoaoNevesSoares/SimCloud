package org.wfparser.tablebuilders;

import org.cloudsimplus.builders.tables.CsvTable;
import org.cloudsimplus.builders.tables.TableBuilderAbstract;
import org.wfparser.tablebuilders.historyentries.HostUsageHistoryEntry;

import java.io.PrintStream;
import java.util.List;

public class HostUsageTableBuilder extends TableBuilderAbstract<HostUsageHistoryEntry> {

    public HostUsageTableBuilder(List<HostUsageHistoryEntry> hostUsageHistoryEntries, PrintStream printStream) {
        super(hostUsageHistoryEntries, new CsvTable().setPrintStream(printStream));
    }

    @Override
    protected void createTableColumns() {
        final var col1 = getTable().newColumn("Time");
        addColumn(col1, HostUsageHistoryEntry::time);

        final var col2 = getTable().newColumn("Datacenter Name");
        addColumn(col2, HostUsageHistoryEntry::dcName);

        final var col3 = getTable().newColumn("Host Name");
        addColumn(col3, HostUsageHistoryEntry::hostName);

        final var col4 = getTable().newColumn("Total Requested PES");
        addColumn(col4, HostUsageHistoryEntry::totalRequestedPes);

        final var col5 = getTable().newColumn("Total Allocated PES");
        addColumn(col5, HostUsageHistoryEntry::totalAllocatedPes);

        final var col6 = getTable().newColumn("Total VMs Active");
        addColumn(col6, HostUsageHistoryEntry::totalVMsActive);

        final var col7 = getTable().newColumn("Requested / Total Load");
        addColumn(col7, HostUsageHistoryEntry::totalLoad);

        final var col8 = getTable().newColumn("Allocated / Total Load");
        addColumn(col8, HostUsageHistoryEntry::activeLoad);

        final var col9 = getTable().newColumn("Allocated / Requested Load");
        addColumn(col9, HostUsageHistoryEntry::activeRequestedRatio);
    }
}

