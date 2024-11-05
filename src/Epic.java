import java.util.ArrayList;

public class Epic extends Task {

    private final ArrayList<Integer> subtasksId = new ArrayList<>();

    public Epic(int id, String name, String description){
        super(id, name, description);
    }

    public Epic(String name, String description){
        super(name, description);
    }

    public void addSubtask(int subtaskId) {
        subtasksId.add(subtaskId);
    }

    public void deleteSubtask(int subtaskId) {
        for (int i = 0; i < subtasksId.size(); i++) {
            if (subtasksId.get(i) == subtaskId) {
                subtasksId.remove(i);
            }
        }
    }

    public void clearSubtasksList() {
        subtasksId.clear();
    }

    public ArrayList<Integer> getSubtasksId(){
        return subtasksId;
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
