package tasks;

import manager.TasksTypes;

import java.time.Duration;
import java.time.Instant;

public class SubTask extends Task {
    private int epicId;

    public SubTask(String name, String description, Status status, Integer epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public SubTask(SubTask subTask) {
        super(subTask.getName(), subTask.getDescription(), subTask.getStatus(), subTask.getStartTime(), subTask.getDuration());
        this.setId(subTask.getId());
        epicId = subTask.getEpicId();
    }


    public SubTask(
            String name,
            String description,
            Status status,
            Instant startTime,
            Duration duration,
            int epicId
    ) {
        super(name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(Integer epicID) {
        this.epicId = epicID;
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
                "%d,%s,%s,%s,%s,%s,,%d,%d",
                getId(),
                TasksTypes.SUBTASK,
                getName(),
                getStatus(),
                getDescription(),
                getStartTime(),
                durationInMinutes,
                getEpicId());
    }
}
