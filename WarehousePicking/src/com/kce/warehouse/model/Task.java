package com.kce.warehouse.model;
public abstract class Task {
    protected String taskId;
    protected String status; 
    public Task(String taskId) {
        this.taskId = taskId;
        this.status = "CREATED";
    }
    public abstract boolean perform();
    public String getTaskId() {
        return taskId;
    }
    public String getStatus() {
        return status;
    }
}

