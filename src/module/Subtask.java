package module;

public class Subtask extends Task {

    private final int epicId;
    protected TaskType type = TaskType.SUBTASK;

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

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,%s,%s,%s,%d", id, type, name, status, description, epicId);
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }
}
