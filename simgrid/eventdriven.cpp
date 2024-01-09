//
// Created by Joao Antonio Soares on 05/01/24.
//

#include <simgrid/s4u.hpp>
#include <utility>
#include "simgrid/plugins/live_migration.h"
#include <random>
XBT_LOG_NEW_DEFAULT_CATEGORY(log,"descriptor");

struct VmInfo {
    int num_of_tasks;
    double load_on_percentage;
};

bool safeForExecution(const simgrid::s4u::ActivityPtr& task) {
    if(task->dependencies_solved() &&
       not task->is_assigned() &&
       task->get_state() != simgrid::s4u::Activity::State::STARTED){
        return true;
    }
    return false;
}
static void worker(const std::vector<simgrid::s4u::ActivityPtr>& dag) {
    int num_of_vms = 3;
    std::uniform_int_distribution<long> dist(0, num_of_vms);
    std::mt19937 rng(14);
    simgrid::s4u::Host* host = simgrid::s4u::Host::by_name("cavalheiro");
    std::vector<simgrid::s4u::VirtualMachine*> vms;
    for(int i=0; i<=num_of_vms; i++) {
        std::string vm_name = "vm" + std::to_string(i);
        simgrid::s4u::VirtualMachine* vm = host->create_vm(vm_name,1);
        vm->start();
        vm->set_data(new VmInfo{0,0.0});
        vms.push_back(vm);
    }
    for(auto& task  : dag) {
        auto* exec = dynamic_cast<simgrid::s4u::Exec*>(task.get());

        exec->on_this_veto_cb([host,vms,&dist,&rng](simgrid::s4u::Exec& exec) {
            // In this simple case, we just assign the child task to a resource when its dependencies are solved
            if (exec.dependencies_solved() && not exec.is_assigned()) {
                exec.set_host(vms[dist(rng)]);
                //exec.set_host(host);
            }
        });
        if(safeForExecution(task)) {
            auto* vm = vms[dist(rng)];
            auto* curr_info = vm->get_data<VmInfo>();
            curr_info->num_of_tasks++;
            curr_info->load_on_percentage = std::min( (double)curr_info->num_of_tasks / vm->get_core_count(), 1.0);
            exec->set_host(vm);
            //exec->set_host(host);
        }
    }
    bool migrate = true;
    simgrid::s4u::ActivitySet set = simgrid::s4u::ActivitySet(dag);
    while(not set.empty()) {
        auto completed_one = set.wait_any();
        if (completed_one != nullptr) {
            auto* exec = dynamic_cast<simgrid::s4u::Exec*>(completed_one.get());
            XBT_INFO("Exec '%s' is complete on %s (start time: %f, finish time: %f) host clock %g",
                     exec->get_cname(),
                     exec->get_host()->get_cname(),
                     exec->get_start_time(), exec->get_finish_time(),
                     exec->get_host()->get_speed());
            if(migrate){
                auto name = exec->get_host()->get_name();
                sg_vm_migrate(simgrid::s4u::Host::by_name("cavalheiro")->vm_by_name_or_null(name),simgrid::s4u::Host::by_name("dubois"));
                migrate = false;
            }
        }
    }
    for(auto& vm : vms) {
        auto* curr_info = vm->get_data<VmInfo>();
        XBT_INFO("VM %s has %d tasks and load of %f",vm->get_cname(),curr_info->num_of_tasks,curr_info->load_on_percentage);
        //delete vm->get_data<VmInfo>();
        //vm->destroy();
    }
}
int main(int argc, char **argv) {
    simgrid::s4u::Engine e(&argc, argv);
    sg_vm_live_migration_plugin_init();
    e.load_platform(argv[1]);
    std::vector<simgrid::s4u::ActivityPtr> dag = simgrid::s4u::create_DAG_from_json("/Users/jansoares/simcloud/simgrid/deployment/seismology/seismologyrecipe-1000.json");
    simgrid::s4u::Actor::create("userCloud",simgrid::s4u::Host::by_name("WmsHost"),worker,dag);
    e.run();
    XBT_INFO("Simulation time %f", simgrid::s4u::Engine::get_instance()->get_clock());
    return 0;
}