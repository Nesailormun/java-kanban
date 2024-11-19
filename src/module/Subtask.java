package module;

public class Subtask extends Task {

    private final int epicId;

    public Subtask(int id, String name, String description, TaskStatus status, int epicId){
        super(id, name, description, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, TaskStatus status, int epicId){
        super(name, description, status);
        this.epicId = epicId;
    }

    public Subtask (String name, String description, int epicId){
        super (name, description);
        this.epicId = epicId;
    }

    public int getEpicId(){
        return epicId;
    }

    @Override
    public String toString() {
        return "module.Subtask{" +
                "epicId=" + epicId +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}