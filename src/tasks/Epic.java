package tasks;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subTasksIds;

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        subTasksIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubTasksIds() {
        return subTasksIds;
    }

    public void addSubTaskById(int id) {
        subTasksIds.add(id);
    }

    public void deleteSubTaskById(int id) {
        subTasksIds.remove(Integer.valueOf(id));
    }

    public void deleteAllSubTasks() {
        subTasksIds.clear();
    }

    @Override
    public String toString() {
        return "\nEpic{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", id=" + getId() +
                ", subTasksIds=" + subTasksIds +
                "}";
    }
}
