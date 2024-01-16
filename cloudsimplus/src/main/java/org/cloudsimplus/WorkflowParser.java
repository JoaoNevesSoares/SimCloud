package org.cloudsimplus;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;

public class WorkflowParser {
    public static Workflow createWorkflow(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            Workflow workflow = mapper.readValue(fileInputStream, Workflow.class);
            resolveDependencies(workflow);
            testifitworks(workflow);
            return workflow;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private static void testifitworks(Workflow workflow) {
        // test if it is working
        for(Activity task: workflow.getTasks()) {
            System.out.println("Task: " + task.getName() + " e " + task.getType() + " e " + task.getRuntimeInSeconds());
            System.out.println("Parents: ");
            for(Activity parent: task.getDependencies()) {
                System.out.println(parent.getName());
            }
            System.out.println("Children: ");
            for(Activity child: task.getSuccessors()) {
                System.out.println(child.getName());
            }
        }
    }
    private static void resolveDependencies(Workflow workflow) {
        var tasks = workflow.getTasks();
        for(Activity task: tasks) {
            var parentsName = task.getParentsName();
            var childrenName = task.getChildrenName();
            for(Activity parent: tasks){
                if(parentsName.contains(parent.getName())) {
                    task.addParent(parent);
                }
            }
            for(Activity child: tasks) {
                if(childrenName.contains(child.getName())){
                    task.addChild(child);
                }
            }
        }
    }
}
