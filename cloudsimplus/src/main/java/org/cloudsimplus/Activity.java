package org.cloudsimplus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cloudsimplus.cloudlets.CloudletSimple;

import java.util.ArrayList;
import java.util.List;

public class Activity extends CloudletSimple {
    private String name;
    private String type;
    private double runtimeInSeconds;
    private List<Activity> parents = new ArrayList<>();
    private List<String> parentsName;
    private List<String> childrenName;
    private List<Activity> children = new ArrayList<>();

    @JsonCreator
    public Activity(@JsonProperty("name") String name,
                    @JsonProperty("type") String type,
                    @JsonProperty("runtimeInSeconds") double runtimeInSeconds,
                    @JsonProperty("cores") int cores,
                    @JsonProperty("parents") List<String> parentsName,
                    @JsonProperty("children") List<String> childrenName) {
        super((long) runtimeInSeconds, cores);
        this.name = name;
        this.type = type;
        this.runtimeInSeconds = runtimeInSeconds;
        this.parentsName = parentsName;
        this.childrenName = childrenName;
    }
    public void setName(String name) {
        this.name=name;

    }
    public String getName() {
        return this.name;
    }
    public void addParent(Activity parent) {
        this.parents.add(parent);
    }
    public void addParents(List<Activity>parents) {
        this.parents.addAll(parents);
    }
    public void addChild(Activity child) {
        this.children.add(child);
    }
    public List<Activity> getDependencies() {
        return this.parents;
    }
    public List<Activity> getSuccessors() {
        return this.children;
    }
    public boolean dependenciesCompleted() {
        boolean completed = true;
        for (Activity parent: this.parents) {
            if(!parent.isFinished()){
                completed = false;
                break;
            }
        }
        return completed;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getRuntimeInSeconds() {
        return runtimeInSeconds;
    }

    public void setRuntimeInSeconds(double runtimeInSeconds) {
        this.runtimeInSeconds = runtimeInSeconds;
    }

    public List<Activity> getParents() {
        return parents;
    }

    public void setParents(List<Activity> parents) {
        this.parents = parents;
    }

    public List<Activity> getChildren() {
        return children;
    }

    public void setChildren(List<Activity> children) {
        this.children = children;
    }

    public List<String> getParentsName() {
        return parentsName;
    }

    public void setParentsName(List<String> parentsName) {
        this.parentsName = parentsName;
    }

    public List<String> getChildrenName() {
        return childrenName;
    }

    public void setChildrenName(List<String> childrenName) {
        this.childrenName = childrenName;
    }
}
