//
// Created by Joao Antonio Soares on 03/01/24.
//
#include <simgrid/s4u.hpp>


XBT_LOG_NEW_DEFAULT_CATEGORY(log,"descriptor");

int main(int argc, char* argv[]) {
    simgrid::s4u::Engine e(&argc, argv);
    e.load_platform(argv[1]);
    simgrid::s4u::Exec::on_completion_cb([](simgrid::s4u::Exec const& exec) {
        XBT_INFO("Exec '%s'",
                 exec.get_cname());
    });
    simgrid::s4u::Host* host = simgrid::s4u::Host::by_name("cavalheiro");
    std::vector<simgrid::s4u::ActivityPtr> dag = simgrid::s4u::create_DAG_from_json("/Users/jansoares/simcloud/simgrid/deployment/montage-2500.json");
    for(auto& task : dag){
        auto *exec = dynamic_cast<simgrid::s4u::Exec *>(task.get());
        exec->set_host(host);
    }
    e.run();
    XBT_INFO("Simulation time %f", simgrid::s4u::Engine::get_instance()->get_clock());
}