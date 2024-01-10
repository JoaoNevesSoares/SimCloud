//
// Created by Joao Antonio Soares on 09/01/24.
//
#include <simgrid/s4u.hpp>
#include "simgrid/plugins/live_migration.h"
#include <random>
XBT_LOG_NEW_DEFAULT_CATEGORY(log,"descriptor");

struct VmInfo {
    int num_of_tasks; //TODO rename to tasks_submitted
    double load_on_percentage;
};

static void updateVmInfo(simgrid::s4u::VirtualMachine* vm ) {
    auto* curr_info = vm->get_data<VmInfo>();
    curr_info->num_of_tasks++;
    curr_info->load_on_percentage = std::min( (double)curr_info->num_of_tasks / vm->get_core_count(), 1.0);
}

class Simulation {
private:
    Simulation() {
    }
    Simulation(const Simulation& simulation) = delete;
    Simulation& operator=(const Simulation& simulation) = delete;
    static Simulation* simulation;
    int scenario;
    static std::mt19937 rng;
public:
    static Simulation* getInstance();
    void setScenario(int num) {
        scenario = num;
    }
    int getScenario() {
        return scenario;
    }
    long getRand(long min, long max) {
        std::uniform_int_distribution<long> dist(min,max);
        return dist(rng);
    }
};

Simulation* Simulation::simulation = nullptr;
std::mt19937 Simulation::rng(14);
Simulation* Simulation::getInstance() {
    if(simulation == nullptr) {
        simulation = new Simulation();
    }
    return simulation;
}


/* An Activity is safe for execution when:
 * - All it's parents finished executing
 * - It's yet not assigned to a resource
 * - (sanity test) It's not started
 * */
bool safeForExecution(const simgrid::s4u::ActivityPtr& task) {
    if(task->dependencies_solved() &&
       not task->is_assigned() &&
       task->get_state() != simgrid::s4u::Activity::State::STARTED) {
        return true;
    }
    return false;
}

// Global variables
//Temporary until I solve the cmakefile linking problem
std::map<simgrid::s4u::Host*, double> hosts_load;
std::map<std::string, std::vector<simgrid::s4u::VirtualMachine*>> vm_list;
simgrid::s4u::Mailbox* platformServiceMailbox = nullptr;

class PlatformService {
public:
    explicit PlatformService() = default;
    explicit PlatformService(const std::vector<std::string>& args) {

        for(auto& host: simgrid::s4u::Engine::get_instance()->get_all_hosts()) {
            if(host->get_name() == "CloudHost"){
                continue;
            }
            addHostLoad(host,0.0);
        }
        /* TODO: parse the xml platform file and create the platform */
        // create a mailbox using the first argument of the actor
        platformServiceMailbox = simgrid::s4u::Mailbox::by_name(args[0]);
    }
    void operator()() const /* This is the main code of the actor */
    {
        XBT_INFO("Hello!");
        simgrid::s4u::Actor::self()->daemonize();
        while(simgrid::s4u::this_actor::get_host()->is_on()) {
            simgrid::s4u::this_actor::sleep_for(10);
        }
    }
    static double getHostLoad(simgrid::s4u::Host* host) {
        return hosts_load[host];
    }
    void addHostLoad(simgrid::s4u::Host* host, double load) {
        hosts_load[host] = load;
    }
    static void updateResourceLoad(simgrid::s4u::VirtualMachine* vm) {
        double old_load_on_percentage = vm->get_data<VmInfo>()->load_on_percentage;
        updateVmInfo(vm);
        double new_load_on_percentage = vm->get_data<VmInfo>()->load_on_percentage;
        double increasing_load = (new_load_on_percentage -  old_load_on_percentage) * vm->get_core_count();
        hosts_load[vm->get_pm()] += increasing_load;
    }
    static simgrid::s4u::VirtualMachine* createVm(std::string username,simgrid::s4u::Host* host, int num_of_cores) {
        std::string vm_name;
        if(!vm_list[username].empty()) {
            vm_name = username + "_vm_" + std::to_string(vm_list[username].size()+1);
        }
        else {
            vm_name = username + "_vm_1";
        }
        simgrid::s4u::VirtualMachine* vm = host->create_vm(vm_name,num_of_cores);
        vm->start();
        // initialize info of vm usage
        vm->set_data(new VmInfo{0,0.0});
        vm_list[username].push_back(vm);
        return vm;
    }
};

//createAndInitializeVMs(host, num_of_vms, num_of_cores)
std::vector<simgrid::s4u::VirtualMachine*> createAndInitializeVMs(const std::string& name, simgrid::s4u::Host* host, int num_of_vms, int num_of_cores) {
    std::vector<simgrid::s4u::VirtualMachine*> vms;
    vms.reserve(num_of_vms);
    for(int i = 0; i < num_of_vms; i++) {
            vms.push_back(PlatformService::createVm(name,host,num_of_cores));
    }
    return vms;
}
static void cloudUser(int argc, char* argv[]) {

    simgrid::s4u::Host* host = simgrid::s4u::this_actor::get_host();

    /* parsing the xml arguments of a specific actor from the deployment file */
    std::string dag_filename = argv[1];
    int num_of_vms = std::stoi(argv[2]);
    int num_of_cores;
    //sanity check on num_of_cores greater than host cores
    if(std::stoi(argv[3]) > host->get_core_count()) {
        num_of_cores = host->get_core_count();
    } else {
        num_of_cores = std::stoi(argv[3]);
    }

    std::string username = argv[4];

    /* Create and start the corresponding number of VMs to this cloud user */
    // VMs are created and started on the host where the cloud user is running
    auto vms = createAndInitializeVMs(username,host,num_of_vms,num_of_cores);
    /* Create a DAG from a json file */
    auto workflow = simgrid::s4u::create_DAG_from_json(dag_filename);

    if(Simulation::getInstance()->getScenario() == 1) {
        /* find root activities in the workflow and schedule then first*/
        auto* vm = vms[0];
        for(auto& task: workflow) {
            auto* exec = dynamic_cast<simgrid::s4u::Exec*>(task.get());
            if(safeForExecution(task)) {
                //Simulation::getInstance()->updateResourceLoad(vm);
                PlatformService::updateResourceLoad(vm);
                //updateVmInfo(vm);
                exec->set_host(vm);
            }
            // add a callback to every activity that hasn't started yet, either by having unsolved dependencies
            // or by not having been assigned to a resource.
            exec->on_this_veto_cb([vm](simgrid::s4u::Exec& exec) {

                // In this simple case, we just assign the child task to a resource when its dependencies are solved
                if (exec.dependencies_solved() && not exec.is_assigned()) {

                    //Simulation::getInstance()->updateResourceLoad(vm);
                    PlatformService::updateResourceLoad(vm);
                    exec.set_host(vm);
                }
            });
        }
    }

    simgrid::s4u::ActivitySet dag = simgrid::s4u::ActivitySet(workflow);
    while(not dag.empty()) {

        //wait for some activity to finish
        auto completed_one = dag.wait_any();
        if(completed_one != nullptr) {
            auto* exec = dynamic_cast<simgrid::s4u::Exec*>(completed_one.get());
            // Log the result of the execution
            XBT_INFO("Exec '%s' is complete on %s (start time: %f, finish time: %f) host clock %g",
                     exec->get_cname(),
                     exec->get_host()->get_cname(),
                     exec->get_start_time(), exec->get_finish_time(),
                     exec->get_host()->get_speed());
            }
    }
    // do a cleanup of allocated resources to this cloud user
    for(auto& virtual_machine : vms) {
        auto* curr_info = virtual_machine->get_data<VmInfo>();
        XBT_INFO("VM %s has %d tasks and load of %f",virtual_machine->get_cname(),curr_info->num_of_tasks,curr_info->load_on_percentage);
        delete curr_info;
        virtual_machine->destroy();
    }
    for(auto& pm : simgrid::s4u::Engine::get_instance()->get_all_hosts()) {
        XBT_INFO("Host %s has load of %f",pm->get_cname(),PlatformService::getHostLoad(pm));
    }
}

int main(int argc, char **argv) {

    simgrid::s4u::Engine e(&argc, argv);

    /* Parsing of the command-line arguments for this simulation */
    if(argc != 4) {
        XBT_INFO("argc %d",argc);
        XBT_INFO("Usage: <xml platform file> <xml cloud scenario file>");
        exit(1);
    }

    /* Set the use case scenario to be simulated
     * TODO: create a method in Simulation class to parse the scenario from the command line
     * */
    auto* simulation = Simulation::getInstance();
    simulation->setScenario(1);
    /* Loading and instantiate the platform from the XML description */
    e.load_platform(argv[2]);
    /* Load the cloud scenario */
    // first, "annotate" the code that will be executed by the cloud actors
    e.register_actor<PlatformService>("platformService");
    e.register_function("cloudUser", cloudUser);
    // then, load the scenario
    e.load_deployment(argv[3]);
    // always load the live migration plugin
    sg_vm_live_migration_plugin_init();
    simgrid::s4u::Engine::get_instance();
    // start the simulation
    e.run();
    XBT_INFO("Simulation time %f", simgrid::s4u::Engine::get_instance()->get_clock());
    return 0;
}