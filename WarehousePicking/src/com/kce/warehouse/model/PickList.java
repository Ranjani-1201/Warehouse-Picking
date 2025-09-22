package com.kce.warehouse.model;

import java.util.ArrayList;
import java.util.List;

public class PickList {
    private String pickListId;
    private List<PickTask> tasks;
    private String status; // CREATED, PARTIAL, COMPLETED

    public PickList(String pickListId) {
        this.pickListId = pickListId;
        this.tasks = new ArrayList<>();
        this.status = "CREATED";
    }

    public String getPickListId() {
        return pickListId;
    }

    public List<PickTask> getTasks() {
        return tasks;
    }

    public void addTask(PickTask t) {
        tasks.add(t);
        updateStatus();
    }

    public void updateStatus() {
        boolean allComplete = true;
        boolean anyStarted = false;
        for (PickTask t : tasks) {
            if (!"COMPLETED".equals(t.getStatus())) {
                allComplete = false;
            }
            if (!"CREATED".equals(t.getStatus())) {
                anyStarted = true;
            }
        }
        if (allComplete) status = "COMPLETED";
        else if (anyStarted) status = "PARTIAL";
        else status = "CREATED";
    }

    public String getStatus() {
        updateStatus();
        return status;
    }
}

