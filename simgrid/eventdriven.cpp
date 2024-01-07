//
// Created by Joao Antonio Soares on 05/01/24.
//

#include "eventdriven.h"
#include <simgrid/s4u.hpp>
#include <utility>
#include "simgrid/plugins/live_migration.h"

XBT_LOG_NEW_DEFAULT_CATEGORY(log,"descriptor");

class Simulation {

public:

    static std::shared_ptr<Simulation> createSimulation() {
        return std::shared_ptr<Simulation>(new Simulation());
    }
    void addHostName(const std::string& hostname) {
        hostnames.push_back(hostname);
    }
    void logHostnames() {
        for(const auto& hostname : hostnames){
            XBT_INFO("Hostname: %s", hostname.c_str());
        }
    }
private:

    std::vector<const std::string> hostnames;

    /**
     * @brief Constructor
     */
    Simulation() {
        XBT_INFO("Simulation constructor");
    }

};
static void jobExecution(const simgrid::s4u::ExecPtr& task,simgrid::s4u::Host* vm,simgrid::s4u::Mailbox* mailbox) {
    task->set_host(vm);
    task->wait();
    XBT_INFO("Exec '%s' is complete on %s (start time: %f, finish time: %f) host clock %g",
             task->get_cname(),
             task->get_host()->get_cname(),
             task->get_start_time(), task->get_finish_time(),
             task->get_host()->get_speed());
    mailbox->put(new int(0),0);
}
static void userCloud(const std::vector<simgrid::s4u::ActivityPtr>& dag, const std::string &mailbox_name) {
    simgrid::s4u::Mailbox* mailbox = simgrid::s4u::Mailbox::by_name(mailbox_name);

    // create virtual machine
    simgrid::s4u::Host* host = simgrid::s4u::Host::by_name("cavalheiro");
    simgrid::s4u::VirtualMachine* vm0 = host->create_vm("vm00", 1);
    simgrid::s4u::VirtualMachine* vm1 = host->create_vm("vm01",1);
    simgrid::s4u::VirtualMachine* vm2 = host->create_vm("vm02",1);
    simgrid::s4u::VirtualMachine* vm3 = host->create_vm("vm03",1);
    vm0->start();
    vm1->start();
    vm2->start();
    vm3->start();
    int last =0;
    std::vector<simgrid::s4u::ActivityPtr> running;
    int counter = 0;
    while(counter < dag.size()) {
        // traverse the dag workflow and find the tasks that are ready to execute
        for (auto &task: dag) {
            // if the task has no dependencies and is not assigned to a host, assign it to a host
            if(task->dependencies_solved() && not task->is_assigned() &&
               task->get_state() != simgrid::s4u::Activity::State::FINISHED) {

                if(auto *exec = dynamic_cast<simgrid::s4u::Exec *>(task.get())) {
                    // create a job to execute this task
                    running.push_back(task);
                    if(last ==0){
                        simgrid::s4u::Actor::create(task->get_cname(),simgrid::s4u::Host::by_name("WmsHost"),jobExecution,exec,vm0,mailbox);
                        last = 1;
                    }
                    else if(last == 1){
                        simgrid::s4u::Actor::create(task->get_cname(),simgrid::s4u::Host::by_name("WmsHost"),jobExecution,exec,vm1,mailbox);
                        last = 2;
                    }
                    else if(last == 2){
                        simgrid::s4u::Actor::create(task->get_cname(),simgrid::s4u::Host::by_name("WmsHost"),jobExecution,exec,vm2,mailbox);
                        last = 3;
                    }
                    else {
                        simgrid::s4u::Actor::create(task->get_cname(), simgrid::s4u::Host::by_name("WmsHost"),
                                                    jobExecution, exec, vm3, mailbox);
                        last = 0;
                    }

                };
            }
        }
        // wait for a job to finish
        mailbox->get<int>();
        // remove from running all jobs that finished
        for(auto it = running.begin(); it != running.end();) {
            auto &task = *it;
            auto *exec = dynamic_cast<simgrid::s4u::Exec *>(task.get());
            if (exec->test()) {
                running.erase(it);
                counter += 1;
                break;
            }
            else{
                it++;
            }
        }
    }
}
int main(int argc, char **argv) {
    simgrid::s4u::Engine e(&argc, argv);
    e.load_platform(argv[1]);
    std::vector<simgrid::s4u::ActivityPtr> dag = simgrid::s4u::create_DAG_from_json("/Users/jansoares/simcloud/simgrid/deployment/montage-250.json");
    simgrid::s4u::Actor::create("userCloud",simgrid::s4u::Host::by_name("WmsHost"),userCloud,dag,std::string ("joao"));
    e.run();
    XBT_INFO("Simulation time %f", simgrid::s4u::Engine::get_instance()->get_clock());
    return 0;
}