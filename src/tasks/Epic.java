package tasks;

import java.util.ArrayList;
public class Epic extends Task {
    ArrayList<Integer> subTasksIds;

    public Epic(Epic newEpic, int id) {
        super(newEpic.name, newEpic.description, newEpic.status, id);
        subTasksIds = newEpic.subTasksIds;
    }

    public Epic(String name, String description, Status status) {
        super(name, description, status);
        subTasksIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubTasksIds() {
        return subTasksIds;
    }

    public void setSubTasksIds(ArrayList<Integer> subTasksIds) {
        this.subTasksIds = subTasksIds;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", id=" + id +
                ", subTasksIds=" + subTasksIds +
                '}';
    }
}
