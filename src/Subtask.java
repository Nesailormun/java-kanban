public class Subtask extends Task {

    private int epicId;

    public Subtask(int id, String name, String description, TaskStatus status, int epicId){
        super(id,name,description,status);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, int epicId){
        super(id, name, description);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int epicId){
        super(name, description);
        this.epicId = epicId;
    }

    public int getEpicId(){
        return epicId;
    }

    public void setEpicId(int newEpicId){
        epicId = newEpicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
