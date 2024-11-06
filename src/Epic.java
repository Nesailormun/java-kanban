import java.util.ArrayList;

public class Epic extends Task {

    private final ArrayList<Integer> subtasksId = new ArrayList<>();

    public Epic(String name, String description){
        super(name, description);
    }

    public Epic (int id, String name, String description){
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

    public ArrayList<Integer> getSubtasksId(){
        return new ArrayList<>(subtasksId);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtasksId=" + subtasksId +
                '}';
    }
}
