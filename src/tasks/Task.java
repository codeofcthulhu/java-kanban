package tasks;

import manager.TasksTypes;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class Task {
    private String name;
    private String description;
    private Status status;
    private Integer id;
    private Instant startTime;
    private Duration duration;

    public Task(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Task(Task task) {
        name = task.getName();
        description = task.getDescription();
        status = task.getStatus();
        id = task.getId();
        startTime = task.getStartTime();
        duration = task.getDuration();
    }

    public Task(String name, String description, Status status, Instant startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Instant getEndTime() {
        Instant endTime = startTime.plus(duration);
        return endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        Long durationInMinutes;
        if (getDuration() == null) {
            durationInMinutes = null;
        } else {
            durationInMinutes = duration.toMinutes();
        }
        return String.format(
                "%d,%s,%s,%s,%s,%s,,%d,",
                id,
                TasksTypes.TASK,
                name,
                status,
                description,
                startTime,
                durationInMinutes
        );
    }
}
