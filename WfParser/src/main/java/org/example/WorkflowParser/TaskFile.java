package org.example.WorkflowParser;

public class TaskFile {

    private String link;
    private String name;
    private long sizeInBytes;

    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public long getSizeInBytes() {
        return sizeInBytes;
    }
    public void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }
}
