package com.warehousewms.model;

import java.util.Date;

public class PickRun {
    private int pickRunId;
    private Integer assignedToUserId;
    private Date startedAt;
    private Date completedAt;
    private String status;

    public int getPickRunId() { return pickRunId; }
    public void setPickRunId(int pickRunId) { this.pickRunId = pickRunId; }

    public Integer getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(Integer assignedToUserId) { this.assignedToUserId = assignedToUserId; }

    public Date getStartedAt() { return startedAt; }
    public void setStartedAt(Date startedAt) { this.startedAt = startedAt; }

    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
