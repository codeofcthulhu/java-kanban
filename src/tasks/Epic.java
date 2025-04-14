package tasks;

import manager.TasksTypes;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subTasksIds;
    private Instant endTime;

    public Epic(String name, String description, Status status, Instant startTime, Instant endTime, Duration duration) {
        super(name, description, status, startTime, duration);
        subTasksIds = new ArrayList<>();
        this.endTime = endTime;
    }

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        subTasksIds = new ArrayList<>();
    }

    public Epic(Epic epic) {
        super(epic.getName(), epic.getDescription(), epic.getStatus(), epic.getStartTime(), epic.getDuration());
        this.setId(epic.getId());
        subTasksIds = epic.getSubTasksIds();
    }

    @Override
    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
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
        subTasksIds.remove(Integer.valueOf(id));
    }

    public void deleteAllSubTasks() {
        subTasksIds.clear();
    }

    @Override
    public String toString() {
        Long durationInMinutes;
        if (getDuration() == null) {
            durationInMinutes = null;
        } else {
            durationInMinutes = getDuration().toMinutes();
        }
        return String.format(
                "%d,%s,%s,%s,%s,%s,%s,%d,",
                getId(),
                TasksTypes.EPIC,
                getName(),
                getStatus(),
                getDescription(),
                getStartTime(),
                getEndTime(),
                durationInMinutes
        );
    }
}
