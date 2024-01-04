//
// Created by Joao Antonio Soares on 03/01/24.
//
#include <simgrid/s4u.hpp>

XBT_LOG_NEW_DEFAULT_CATEGORY(log,"descriptor");

static void list_all(const std::vector<simgrid::s4u::ActivityPtr>& dag, simgrid::s4u::Host* host) {

    for(auto& task: dag){
        auto *exec = dynamic_cast<simgrid::s4u::Exec *>(task.get());
        exec->set_host(host);
    }
}
int main(int argc, char* argv[]) {
    simgrid::s4u::Engine e(&argc, argv);
    e.load_platform(argv[1]);
    //std::set<simgrid::s4u::Activity*> vetoed;
    //e.track_vetoed_activities(&vetoed);
    simgrid::s4u::Exec::on_completion_cb([](simgrid::s4u::Exec const& exec) {
        XBT_INFO("Exec '%s' is complete on %s (start time: %f, finish time: %f) host clock %g",
                 exec.get_cname(),
                 exec.get_host()->get_cname(),
                 exec.get_start_time(), exec.get_finish_time(),
                 exec.get_host()->get_speed());
    });
    simgrid::s4u::Exec::on_veto_cb([](simgrid::s4u::Exec const& exec) {
        XBT_INFO("task %s is vetoed",exec.get_cname());
    });
    std::vector<simgrid::s4u::ActivityPtr> dag = simgrid::s4u::create_DAG_from_json("/Users/jansoares/simcloud/simgrid/deployment/epigenomics-43.json");
    list_all(dag,simgrid::s4u::Host::by_name("cavalheiro"));
    std::vector<simgrid::s4u::ActivityPtr> dug = simgrid::s4u::create_DAG_from_json("/Users/jansoares/simcloud/simgrid/deployment/sipht.json");
    list_all(dug,simgrid::s4u::Host::by_name("cavalheiro"));
    e.run();
}