//
// Created by Joao Antonio Soars on 01/01/24.
//
#include <simgrid/s4u.hpp>

XBT_LOG_NEW_DEFAULT_CATEGORY(log,"descriptor");

static void Wms(std::vector<simgrid::s4u::ActivityPtr> dag) {

    // Obtain the host available to execute the workflow
    simgrid::s4u::Host* host = simgrid::s4u::Host::by_name("cavalheiro");

    simgrid::s4u::ActivitySet running_waiting;
    std::vector<simgrid::s4u::ActivityPtr> ready_tasks;
    while(not dag.empty()) {

        // traverse the dag workflow and find the tasks that are ready to execute
        for (auto &task: dag) {
            // if the task has no dependencies and is not assigned to a host, assign it to a host
            if(task->dependencies_solved() && not task->is_assigned()){

                if(auto *exec = dynamic_cast<simgrid::s4u::Exec *>(task.get())){
                    exec->set_host(host);
                }

                //assign task to running_waiting set so it is possible to make this actor wait until the task finishes
                running_waiting.push(task);
                //assign task to ready list to track when it finishes
                ready_tasks.push_back(task);
            }
        }

        // wait for any task that is independent of the others to finish
        running_waiting.wait_any();

        // find the task that finished, and remove from running_waiting set;
        for (auto &task: ready_tasks) {
            auto *exec = dynamic_cast<simgrid::s4u::Exec *>(task.get());
            if (exec->test()) {
                dag.erase(std::remove(dag.begin(), dag.end(), task), dag.end());
                ready_tasks.erase(std::remove(ready_tasks.begin(), ready_tasks.end(), task), ready_tasks.end());
                break;
            }
        }
    }
}

int main(int argc, char* argv[]) {

    simgrid::s4u::Engine e(&argc, argv);
    e.load_platform(argv[1]);

    // Log the completion of all task executed
    simgrid::s4u::Exec::on_completion_cb([](simgrid::s4u::Exec const& exec) {
        XBT_INFO("Exec '%s' is complete on %s (start time: %f, finish time: %f) host clock %g",
                 exec.get_cname(),
                 exec.get_host()->get_cname(),
                 exec.get_start_time(), exec.get_finish_time(),
                 exec.get_host()->get_speed());
    });
    std::vector<simgrid::s4u::ActivityPtr> dag = simgrid::s4u::create_DAG_from_json("/Users/jansoares/simcloud/simgrid/deployment/epigenomics-529.json");
    simgrid::s4u::Actor::create("worker", simgrid::s4u::Host::by_name("cavalheiro"), Wms,dag);
    e.run();
    XBT_INFO("Simulation time %f", simgrid::s4u::Engine::get_instance()->get_clock());
}