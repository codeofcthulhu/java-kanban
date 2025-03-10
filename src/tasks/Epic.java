package tasks;

import manager.TasksTypes;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subTasksIds;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        subTasksIds = new ArrayList<>();
    }

    public Epic(Epic epic) {
        super(epic.getName(), epic.getDescription(), epic.getStatus());
        this.setId(epic.getId());
        subTasksIds = epic.getSubTasksIds();
    }

    public ArrayList<Integer> getSubTasksIds() {
        return subTasksIds;
    }

    public void setSubTasksIds(ArrayList<Integer> subTasksIds) {
        this.subTasksIds = subTasksIds;
    }

    public void addSubTaskById(int id) {
        subTasksIds.add(id);
    }

    public void deleteSubTaskById(int id) {
        subTasksIds.remove(id);
    }

    public void deleteAllSubTasks() {
        subTasksIds.clear();
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,", getId(), TasksTypes.TASK, getName(), getStatus(), getDescription());
    }
}
