package org.example;

import org.cloudsimplus.builders.tables.CsvTable;
import org.cloudsimplus.builders.tables.TableBuilderAbstract;

import java.io.PrintStream;
import java.util.List;

public class RequestsTableBuilder extends TableBuilderAbstract<RequestsStateHistoryEntry> {

    public RequestsTableBuilder(List<RequestsStateHistoryEntry> requestsStateHistoryEntries) {
        super(requestsStateHistoryEntries);
    }
    public RequestsTableBuilder(List<RequestsStateHistoryEntry> requestsStateHistoryEntries, PrintStream printStream) {
        super(requestsStateHistoryEntries, new CsvTable().setPrintStream(printStream));
    }
    @Override
    protected void createTableColumns() {

        final var col1 = getTable().newColumn("Time");
        addColumn(col1, RequestsStateHistoryEntry::time);

        final var col2 = getTable().newColumn("Total Users");
        addColumn(col2, RequestsStateHistoryEntry::usersAmount);

        final var col3 = getTable().newColumn("Users Active");
        addColumn(col3, RequestsStateHistoryEntry::usersActive);

        final var col4 = getTable().newColumn("Finished Users");
        addColumn(col4, RequestsStateHistoryEntry::usersFinishedAmount);

        final var col5 = getTable().newColumn("VMs Created");
        addColumn(col5, RequestsStateHistoryEntry::vmsAllocatedAmount);

        final var col6 = getTable().newColumn("VMs Failed");
        addColumn(col6, RequestsStateHistoryEntry::vmsFailedAmount);

        final var col7 = getTable().newColumn("VMs Active");
        addColumn(col7, RequestsStateHistoryEntry::vmsCurrentActive);
    }
}
