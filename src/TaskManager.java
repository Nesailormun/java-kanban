import java.util.ArrayList;
import java.util.List;


public interface TaskManager {

    public List<Task> getHistory();
    // Beginning of Task methods:

    public Task getTaskById(int id);


    public Task createTask(Task task);



    public void updateTask(Task task);

    public void removeTask (int id);

    public void deleteAllTasks ();

    public ArrayList<Task> getAllTasks();


    // Beginning of Epic methods:

    public Epic createEpic(Epic epic);

    public void updateEpic(Epic epic);

    public Epic getEpicById(int id);

    public ArrayList<Epic> getAllEpics();

    public void removeEpic (int id);

    public void deleteAllEpics();


    public ArrayList<Subtask> getEpicsSubtasks(Epic epic);


    // Beginning of Subtask methods:

    public Subtask createSubtask(Subtask subtask);

    public void updateSubtask(Subtask subtask);

    public Subtask getSubtaskById (int id);

    public ArrayList<Subtask> getAllSubtasks();

    public void removeSubtask (int id);

    public void deleteAllSubtasks ();
}

