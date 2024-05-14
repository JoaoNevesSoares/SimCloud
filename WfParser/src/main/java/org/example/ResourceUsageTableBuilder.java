package org.example;

import org.cloudsimplus.builders.tables.CsvTable;
import org.cloudsimplus.builders.tables.TableBuilderAbstract;

import java.io.PrintStream;
import java.util.List;

public class ResourceUsageTableBuilder extends TableBuilderAbstract<ResourceUsageHistoryEntry> {

    public ResourceUsageTableBuilder(List<ResourceUsageHistoryEntry> resourceUsageHistoryEntries) {
        super(resourceUsageHistoryEntries);
    }
    public ResourceUsageTableBuilder(List<ResourceUsageHistoryEntry> resourceUsageHistoryEntries, PrintStream printStream) {
        super(resourceUsageHistoryEntries, new CsvTable().setPrintStream(printStream));
    }
    @Override
    protected void createTableColumns() {
        // TODO: TOTAL REQUESTED PES
        // TODO: TOTAL REQUESTED MIPS
        // TODO: TOTAL ALLOCATED PES
        // TODO: TOTAL ALLOCATED MIPS
        // TODO: TOTAL HOST ACTIVE
        // TODO: TOTAL USAGE MIPS

        final var col1 = getTable().newColumn("Time");
        addColumn(col1, ResourceUsageHistoryEntry::time);

        final var col2 = getTable().newColumn("Total Requested PES");
        addColumn(col2, ResourceUsageHistoryEntry::totalRequestedPes);

        final var col3 = getTable().newColumn("Total Requested MIPS");
        addColumn(col3, ResourceUsageHistoryEntry::totalRequestedMips);

        final var col4 = getTable().newColumn("Total Allocated PES");
        addColumn(col4, ResourceUsageHistoryEntry::totalAllocatedPes);

        final var col5 = getTable().newColumn("Total Allocated MIPS");
        addColumn(col5, ResourceUsageHistoryEntry::totalAllocatedMips);

        final var col6 = getTable().newColumn("Total Hosts Active");
        addColumn(col6, ResourceUsageHistoryEntry::totalHostsActive);
        final var col7 = getTable().newColumn("Total MIPS Usage");
    }
}
