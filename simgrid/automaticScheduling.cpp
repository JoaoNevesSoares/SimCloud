//
// Created by Joao Antonio Soares on 03/01/24.
//
#include <simgrid/s4u.hpp>
#include "simgrid/plugins/live_migration.h"

XBT_LOG_NEW_DEFAULT_CATEGORY(log,"descriptor");


static void migrater(simgrid::s4u::VirtualMachine* vm) {
    simgrid::s4u::Actor::self()->daemonize();
    sg_vm_migrate(vm,simgrid::s4u::Host::by_name("dubois"));
    simgrid::s4u::this_actor::sleep_for(10);
    XBT_INFO("migrater");
}
bool safeForExecution(const simgrid::s4u::ActivityPtr& task) {
    if(task->dependencies_solved() &&
       not task->is_assigned() &&
       task->get_state() != simgrid::s4u::Activity::State::STARTED){
        return true;
    }
    return false;
}
int main(int argc, char* argv[]) {
    simgrid::s4u::Engine e(&argc, argv);
    sg_vm_live_migration_plugin_init();
    e.load_platform(argv[1]);
    simgrid::s4u::Exec::on_completion_cb([](simgrid::s4u::Exec const& exec) {
        XBT_INFO("Exec '%s' is complete on %s (start time: %f, finish time: %f)",
                 exec.get_cname(),
                 exec.get_host()->get_cname(),
                 exec.get_start_time(), exec.get_finish_time());
    });
    simgrid::s4u::Host* host = simgrid::s4u::Host::by_name("cavalheiro");
    simgrid::s4u::VirtualMachine* vm0 = host->create_vm("vm0",4);
    vm0->start();
    std::vector<simgrid::s4u::ActivityPtr> dag = simgrid::s4u::create_DAG_from_json("/Users/jansoares/simcloud/simgrid/deployment/epigenomics-ordenado.json");
    for(auto& task  : dag) {
        auto* exec = dynamic_cast<simgrid::s4u::Exec*>(task.get());
        exec->set_host(vm0);
    }
    e.run();
    XBT_INFO("Simulation time %f", simgrid::s4u::Engine::get_instance()->get_clock());
}