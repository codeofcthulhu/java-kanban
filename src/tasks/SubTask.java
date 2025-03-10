package tasks;

import manager.TasksTypes;

public class SubTask extends Task {
    private int epicId;

    public SubTask(String name, String description, Status status, Integer epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public SubTask(SubTask subTask) {
        super(subTask.getName(), subTask.getDescription(), subTask.getStatus());
        this.setId(subTask.getId());
        epicId = subTask.getEpicId();
    }

    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(Integer epicID) {
        this.epicId = epicID;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%d", getId(), TasksTypes.SUBTASK, getName(), getStatus(), getDescription(), getEpicId());
    }
}
