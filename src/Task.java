import java.util.Objects;

public class Task {

    protected int id;
    protected String name;
    protected String description;
    protected TaskStatus status;

    public Task(int id, String name, String description, TaskStatus status){
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Task(int id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
        status = TaskStatus.NEW;
    }

    public Task (String name, String description){
        this.name = name;
        this.description = description;
        status = TaskStatus.NEW;
    }

    public int getId(){
        return id;
    }

    public TaskStatus getStatus(){
        return status;
    }
    public void setStatus(TaskStatus newStatus){
        status = newStatus;
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

    public void setId (int newId){
        id = newId;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id &&
                Objects.equals(name, task.name) &&
                Objects.equals(description, task.description) &&
                status == task.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, status);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
