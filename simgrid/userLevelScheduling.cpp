//
// Created by Joao Antonio Soares on 01/01/24.
//
#include <simgrid/s4u.hpp>
#include "simgrid/plugins/load.h"

XBT_LOG_NEW_DEFAULT_CATEGORY(log,"descrição");

static void monitor(simgrid::s4u::Mailbox* mailbox) {

    simgrid::s4u::Actor::self()->daemonize();
    while(simgrid::s4u::this_actor::get_host()->is_on()){
        auto msg = *mailbox->get<std::string>();
        XBT_INFO("recebi %s", msg.c_str());
    }
}

class UserPlatform {

public:
    std::vector<simgrid::s4u::Host*> hosts_list;
    std::vector<simgrid::s4u::VirtualMachine*> vms_list;

    explicit UserPlatform(const std::vector<std::string>& hosts_names){
        for(auto& host_name : hosts_names){
            hosts_list.push_back(simgrid::s4u::Host::by_name(host_name));
        }
    }
    void createVM(simgrid::s4u::Host* pm, int num_cores, const std::string& vm_name){
        simgrid::s4u::VirtualMachine* vm = pm->create_vm(vm_name, num_cores, 1024);
        vms_list.push_back(vm);
    }
};

class WMS {

public:

    mutable std::vector<simgrid::s4u::ActivityPtr> dag;
    mutable UserPlatform* user_platform;
    mutable std::string my_name;
    explicit WMS(UserPlatform* user_platform = nullptr): user_platform(user_platform){
        XBT_INFO("WMS created");
    }
    void addDag(const std::string& dag_file){
        XBT_INFO("dag path: %s", dag_file.c_str());
        this->dag = simgrid::s4u::create_DAG_from_json(dag_file);
    }
    static void schedule_task_on(simgrid::s4u::Exec* task, simgrid::s4u::VirtualMachine* vm){
        task->set_host(vm);
    }
    void setName(std::string name){
        this->my_name = name;
    }
    void start() {
        std::mutex mailboxMutex;
        // start all tasks in the same vm of the same host
        for (const auto &task : this->dag) {
            auto *exec = dynamic_cast<simgrid::s4u::Exec *>(task.get());
            if (task != nullptr) {
                schedule_task_on(exec, user_platform->vms_list[0]);
                std::string msg = "task_start" + this->my_name;
                simgrid::s4u::Mailbox::by_name("cavalheiro")->put(&msg, 0);
            }
        }
        for (const auto &task : this->dag) {
            auto* exec = dynamic_cast<simgrid::s4u::Exec*>(task.get());
            exec->wait();
        }
        for (const auto& vm: user_platform->vms_list) {
            vm->destroy();
        }
    }
};


static void user(int argc, char* argv[]) {

    std::string dag_file_name = argv[1];
    int num_of_vms_available = std::stoi(argv[2]);
    int num_of_cores = std::stoi(argv[3]);
    std::string user_name = argv[4];
    auto* plat = new UserPlatform({"cavalheiro"});
    plat->createVM(simgrid::s4u::Host::by_name("cavalheiro"), num_of_cores, user_name + "_vm");
    auto* pegasus = new WMS(plat);
    pegasus->setName(user_name);
    pegasus->addDag(dag_file_name);
    pegasus->start();
    delete plat;
    delete pegasus;
}

static void accountExecution(simgrid::s4u::Exec const & exec){
    XBT_INFO("Exec '%s' is complete on %s (start time: %f, finish time: %f) host clock %g",
             exec.get_cname(),
             exec.get_host()->get_cname(),
             exec.get_start_time(), exec.get_finish_time(),
             exec.get_host()->get_speed());
}

int main(int argc, char* argv[]) {

    simgrid::s4u::Engine e(&argc, argv);
    e.load_platform(argv[1]);
    e.register_function("user", user);
    e.load_deployment(argv[2]);
    simgrid::s4u::Exec::on_completion_cb(accountExecution);
    //create monitor actor for each host
    for(auto& host : simgrid::s4u::Engine::get_instance()->get_all_hosts()){
        simgrid::s4u::Actor::create(host->get_name(),host, monitor, simgrid::s4u::Mailbox::by_name(host->get_cname()));
    }
    e.run();
    XBT_INFO("Simulation time %g", e.get_clock());
}