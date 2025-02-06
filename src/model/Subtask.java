package model;

import enums.TaskStatus;
import enums.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {

    private final int epicId;

    public Subtask(int id, String name, String description, TaskStatus status, int epicId) {
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, TaskStatus status, int epicId) {
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, TaskStatus status, int epicId, LocalDateTime startTime,
                   Duration duration) {
        super(id, name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, TaskStatus status, int epicId, LocalDateTime startTime,
                   Duration duration) {
        super(name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int epicId, LocalDateTime startTime, Duration duration) {
        super(name, description, startTime, duration);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        if (startTime == null)
            return String.format("%d,%s,%s,%s,%s,%d", id, getType(), name, status, description, epicId);
        return String.format("%d,%s,%s,%s,%s,%s,%d,%d", id, getType(), name, status, description, startTime,
                getDuration().toMinutes(), epicId);

    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }
}
