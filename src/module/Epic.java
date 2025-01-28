package module;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Formatter;

public class Epic extends Task {

    private final ArrayList<Integer> subtasksId = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(int id, String name, String description) {
        super(name, description);
        this.id = id;
    }

    public void addSubtask(int subtaskId) {
        subtasksId.add(subtaskId);
    }

    public void deleteSubtask(int subtaskId) {
        subtasksId.remove(Integer.valueOf(subtaskId));
    }

    public void clearSubtasksList() {
        subtasksId.clear();
    }

    public ArrayList<Integer> getSubtasksId() {
        return new ArrayList<>(subtasksId);
    }


    @Override
    public LocalDateTime getEndTime(){
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime){
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        if (startTime == null)
            return String.format("%d,%s,%s,%s,%s", id, getType(), name, status, description);
        return String.format("%d,%s,%s,%s,%s,%s,%d", id, getType(), name, status, description, startTime,
                getDuration().toMinutes());
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }
}
