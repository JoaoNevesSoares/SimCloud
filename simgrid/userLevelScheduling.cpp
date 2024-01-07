//
// Created by Joao Antonio Soars on 01/01/24.
//
#include <simgrid/s4u.hpp>
#include "simgrid/plugins/live_migration.h"
XBT_LOG_NEW_DEFAULT_CATEGORY(log,"descriptor");

bool safeForExecution(const simgrid::s4u::ActivityPtr& task) {
    if(task->dependencies_solved() &&
       not task->is_assigned() &&
       task->get_state() != simgrid::s4u::Activity::State::STARTED){
        return true;
    }
    return false;
}
static void jobExecution(simgrid::s4u::Exec* task,simgrid::s4u::Mailbox* mailbox) {
    task->wait();
    XBT_INFO("Exec '%s' is complete on %s (start time: %f, finish time: %f)",
             task->get_cname(),
             task->get_host()->get_cname(),
             task->get_start_time(), task->get_finish_time());
    mailbox->put(task,0);
}

static void userCloud(const std::vector<simgrid::s4u::ActivityPtr>& dag, const std::string &mailbox_name) {

    simgrid::s4u::Mailbox* mailbox = simgrid::s4u::Mailbox::by_name(mailbox_name);
    simgrid::s4u::Host* host = simgrid::s4u::Host::by_name("cavalheiro");
    simgrid::s4u::VirtualMachine* vm0 = host->create_vm("vm00", 4);
    vm0->start();
    std::vector<simgrid::s4u::Exec*> ready;
    std::vector<simgrid::s4u::Exec*> pending;
    for(auto& task  : dag) {
        auto* exec = dynamic_cast<simgrid::s4u::Exec*>(task.get());
        if(safeForExecution(task)) {
            ready.push_back(exec);
        }
    }

    while(not ready.empty()) {

        for(auto& task : ready) {

            for(auto& dependency : task->get_successors()){
                auto* exec = dynamic_cast<simgrid::s4u::Exec*>(dependency.get());

                // adicione a lista de tarefas pendentes apenas se não estiver pendente ou ready
                if(std::find(ready.begin(), ready.end(),exec) == ready.end() &&
                   std::find(pending.begin(), pending.end(),exec) == pending.end()){
                    pending.push_back(exec);
                }
            }
            if(not task->is_assigned()){
                task->set_host(vm0);
                simgrid::s4u::Actor::create(task->get_cname(),simgrid::s4u::Host::by_name("WmsHost"),jobExecution,task,mailbox);
            }
        }

        //wait for execution to finish
        auto finished_task =  mailbox->get<simgrid::s4u::Exec>();

        ready.erase(std::remove(ready.begin(), ready.end(), finished_task), ready.end());
        auto it = pending.begin();
        while (it != pending.end()) {
            if (safeForExecution(*it) &&
                std::find(ready.begin(), ready.end(), *it) == ready.end()) { // Verifica se já não está em ready
                ready.push_back(*it);
                it = pending.erase(it);
            } else {
                ++it;
            }
        }
    }
    vm0->destroy();
}
int main(int argc, char* argv[]) {
    simgrid::s4u::Engine e(&argc, argv);
    e.load_platform(argv[1]);
    std::vector<simgrid::s4u::ActivityPtr> dag = simgrid::s4u::create_DAG_from_json("/Users/jansoares/simcloud/simgrid/deployment/montage-2500.json");
    simgrid::s4u::Actor::create("userCloud",simgrid::s4u::Host::by_name("WmsHost"),userCloud,dag,std::string ("joao"));
    e.run();
    XBT_INFO("Simulation time %f", simgrid::s4u::Engine::get_instance()->get_clock());
}