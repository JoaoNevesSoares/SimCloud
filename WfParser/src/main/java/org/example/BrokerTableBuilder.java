package org.example;

import org.cloudsimplus.builders.tables.CsvTable;
import org.cloudsimplus.builders.tables.TableBuilderAbstract;

import java.io.PrintStream;
import java.util.List;

public class BrokerTableBuilder extends TableBuilderAbstract<BrokerHistoryEntry> {

    public BrokerTableBuilder(List<BrokerHistoryEntry> brokerHistoryEntries, PrintStream printStream) {
        super(brokerHistoryEntries, new CsvTable().setPrintStream(printStream));
    }
    @Override
    protected void createTableColumns() {
        final var col1 = getTable().newColumn("Broker Name");
        addColumn(col1, BrokerHistoryEntry::brokerName);
        final var col2 = getTable().newColumn("Workflow Name");
        addColumn(col2, BrokerHistoryEntry::workflowName);
        final var col3 = getTable().newColumn("Start Time");
        addColumn(col3, BrokerHistoryEntry::startTime);
        final var col4 = getTable().newColumn("Finish Time");
        addColumn(col4, BrokerHistoryEntry::finishTime);
        final var col5 = getTable().newColumn("Workflow Makespan");
        addColumn(col5, BrokerHistoryEntry::makespan);
        final var col6 = getTable().newColumn("Average Task Makespan");
        addColumn(col5, BrokerHistoryEntry::averageTaskMakespan);
        final var col7 = getTable().newColumn("Submitted Tasks");
        addColumn(col7, BrokerHistoryEntry::submittedTasks);
        final var col8 = getTable().newColumn("Completed Tasks");
        addColumn(col8, BrokerHistoryEntry::completedTasks);
        final var col9 = getTable().newColumn("Failed Tasks");
        addColumn(col9, BrokerHistoryEntry::failedTasks);
    }
}
