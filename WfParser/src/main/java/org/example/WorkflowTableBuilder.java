package org.example;

import org.cloudsimplus.builders.tables.CsvTable;
import org.cloudsimplus.builders.tables.MarkdownTable;
import org.cloudsimplus.builders.tables.TableBuilderAbstract;
import org.example.WorkflowParser.CloudletWorkflow;

import java.io.PrintStream;
import java.util.List;

public class WorkflowTableBuilder extends TableBuilderAbstract<CloudletWorkflow> {
    public static final String DEF_FORMAT = "%s";

    private static final String SECONDS = "Seconds";
    private static final String CPU_CORES = "CPU cores";
    private static final String ID = "ID";
    private static final String MI = "MI";

    /**
     * The format for time columns.
     */
    private String timeFormat = "%.1f";

    /**
     * The format for ID columns.
     */
    private String idFormat = DEF_FORMAT;

    /**
     * The format for cloudlet length columns.
     */
    private String lengthFormat = DEF_FORMAT;

    /**
     * The format for columns indicating number of PEs.
     */
    private String peFormat = DEF_FORMAT;


    public WorkflowTableBuilder(final List<? extends CloudletWorkflow> workflows) {
        super(workflows);
    }
    public WorkflowTableBuilder(final List<? extends CloudletWorkflow> workflows, PrintStream ps) {
        //super(workflows,new CsvTable().setPrintStream(ps));
        super(workflows, new MarkdownTable().setPrintStream(ps));
    }
    @Override
    protected void createTableColumns() {
        addColumn(getTable().newColumn("Broker", ID), cloudlet -> cloudlet.getBroker().getName());
        addColumn(getTable().newColumn("         Name         "), CloudletWorkflow::getName);
        // 1 extra space to ensure proper formatting
        addColumn(getTable().newColumn(" Status") , cloudlet -> cloudlet.getStatus().name());

        addColumn(getTable().newColumn("  DC  ",ID, idFormat), cloudlet -> cloudlet.getVm().getHost().getDatacenter().getName());

        addColumn(getTable().newColumn("Host", ID, idFormat), cloudlet -> cloudlet.getVm().getHost().getId());
        addColumn(getTable().newColumn("Host PEs ", CPU_CORES, peFormat), cloudlet -> cloudlet.getVm().getHost().getWorkingPesNumber());

        addColumn(getTable().newColumn("VM", ID, idFormat), cloudlet -> cloudlet.getVm().getId());

        // 3 extra spaces to ensure proper formatting
        addColumn(getTable().newColumn("   VM PEs", CPU_CORES, peFormat), cloudlet -> cloudlet.getVm().getPesNumber());
        addColumn(getTable().newColumn("   VM MIPS", MI, timeFormat), cloudlet -> cloudlet.getVm().getMips());
        addColumn(getTable().newColumn("ActivityLen", MI, lengthFormat), CloudletWorkflow::getLength);
        addColumn(getTable().newColumn("StartTime", SECONDS, timeFormat), CloudletWorkflow::getStartTime);
        addColumn(getTable().newColumn("FinishTime", SECONDS, timeFormat), CloudletWorkflow::getFinishTime);
        addColumn(getTable().newColumn("ExecTime", SECONDS, timeFormat), CloudletWorkflow::getTotalExecutionTime);
    }


}
