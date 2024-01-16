package org.cloudsimplus;


import org.cloudsimplus.builders.tables.MarkdownTable;
import org.cloudsimplus.builders.tables.Table;
import org.cloudsimplus.builders.tables.TableBuilderAbstract;
import org.cloudsimplus.core.Identifiable;

import java.util.List;

import static java.util.Objects.requireNonNullElse;

public class ActivityTableBuilder extends TableBuilderAbstract<Activity> {
    public static final String DEF_FORMAT = "%d";

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

    /**
     * Instantiates a builder to print the list of Cloudlets using the
     * default {@link MarkdownTable}.
     * To use a different {@link Table}, check the alternative constructors.
     *
     * @param list the list of Cloudlets to print
     */
    public ActivityTableBuilder(final List<? extends Activity> list) {
        super(list);
    }

    /**
     * Instantiates a builder to print the list of Cloudlets using the
     * given {@link Table}.
     *
     * @param list the list of Cloudlets to print
     * @param table the {@link Table} used to build the table with the Cloudlets data
     */
    public ActivityTableBuilder(final List<? extends Activity> list, final Table table) {
        super(list, table);
    }

    @Override
    protected void createTableColumns() {
        addColumn(getTable().newColumn("Activity", ID), Identifiable::getId);
        addColumn(getTable().newColumn("  Name  "), Activity::getName);

        // 1 extra space to ensure proper formatting
        addColumn(getTable().newColumn(" Status") , cloudlet -> cloudlet.getStatus().name());

        addColumn(getTable().newColumn("DC", ID, idFormat), cloudlet -> cloudlet.getVm().getHost().getDatacenter().getId());

        addColumn(getTable().newColumn("Host", ID, idFormat), cloudlet -> cloudlet.getVm().getHost().getId());
        addColumn(getTable().newColumn("Host PEs ", CPU_CORES, peFormat), cloudlet -> cloudlet.getVm().getHost().getWorkingPesNumber());

        addColumn(getTable().newColumn("VM", ID, idFormat), cloudlet -> cloudlet.getVm().getId());

        // 3 extra spaces to ensure proper formatting
        addColumn(getTable().newColumn("   VM PEs", CPU_CORES, peFormat), cloudlet -> cloudlet.getVm().getPesNumber());
        addColumn(getTable().newColumn("ActivityLen", MI, lengthFormat), Activity::getLength);
        addColumn(getTable().newColumn("FinishedLen", MI, lengthFormat), Activity::getFinishedLengthSoFar);
        addColumn(getTable().newColumn("ActivityPEs", CPU_CORES, peFormat), Activity::getPesNumber);
        addColumn(getTable().newColumn("StartTime", SECONDS, timeFormat), Activity::getStartTime);
        addColumn(getTable().newColumn("FinishTime", SECONDS, timeFormat), Activity::getFinishTime);
        addColumn(getTable().newColumn("ExecTime", SECONDS, timeFormat), Activity::getTotalExecutionTime);
    }

    /**
     * Sets the format for time columns.
     */
    public ActivityTableBuilder setTimeFormat(final String timeFormat) {
        this.timeFormat = requireNonNullElse(timeFormat, "");
        return this;
    }

    /**
     * Sets the format for cloudlet length columns.
     */
    public ActivityTableBuilder setLengthFormat(final String lengthFormat) {
        this.lengthFormat = requireNonNullElse(lengthFormat, "");
        return this;
    }

    /**
     * Sets the format for ID columns.
     */
    public ActivityTableBuilder setIdFormat(final String idFormat) {
        this.idFormat = requireNonNullElse(idFormat, "");
        return this;
    }

    /**
     * Sets the format for columns indicating number of PEs.
     */
    public ActivityTableBuilder setPeFormat(final String peFormat) {
        this.peFormat = requireNonNullElse(peFormat, "");
        return this;
    }
}