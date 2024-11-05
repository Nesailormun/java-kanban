import java.util.HashMap;
import java.util.ArrayList;

public class TaskManager {

    private int taskId = 1;
    private int epicId = 1;
    private int subtaskId = 1;
    private final HashMap<Integer, Task> taskStorage = new HashMap<>();
    private final HashMap<Integer, Epic> epicStorage = new HashMap<>();
    private final HashMap<Integer, Subtask> subtaskStorage = new HashMap<>();

    // Beginning of Task methods:

    public Task getTaskById(int id) {
        if (taskStorage.containsKey(id)) {
            System.out.println("Ваша задача найдена!");
            return taskStorage.get(id);
        }
        System.out.println("Такой задачи у нас нет!");
        return null;
    }

    public Task createTask(Task task) {
        if(task == null){
            return null;
        }
        task.setStatus(TaskStatus.NEW);
        if (task.getId() == 0) {
            task.setId(taskId);
        }
        taskStorage.put(task.id, task);
        taskId++;
        return task;
    }

    public void updateTask(Task task){
        if(task == null){
            return;
        }
        if (taskStorage.containsKey(task.getId())){
            taskStorage.put(task.getId(), task);
        } else {
            System.out.println("Задачи с таким номером нет!");
        }
    }

    public void removeTask (int id){
        System.out.println("Задача: " + taskStorage.get(id).name + " удалена!");
        taskStorage.remove(id);
    }

    public void deleteAllTasks () {
        if (taskId != 1) {
            taskStorage.clear();
            taskId = 1;
            System.out.println("Список задач очищен!");
        } else {
            System.out.println("Трекер задач пуст!");
        }
    }

    public ArrayList<Task> getAllTasks(){
        if (taskId != 1) {
            return new ArrayList<Task>(taskStorage.values());
        }
        return null;
    }

    // Beginning of Epic methods:

    public Epic createEpic(Epic epic){
        if(epic == null){
            return null;
        }
        if (epic.getId() == 0) {
            epic.setId(epicId);
        }
        epic.setStatus(calculateEpicStatus(epic));
        epicStorage.put(epic.getId(), epic);
        epicId++;
        return epic;
    }

    public void updateEpic(Epic epic){
        if(epic == null){
            return;
        }
        if (epicStorage.containsKey(epic.getId())){
            epicStorage.get(epic.getId()).setName(epic.getName());
            epicStorage.get(epic.getId()).setDescription(epic.getDescription());
            epic.setStatus(calculateEpicStatus(epic));
        } else {
            System.out.println("Эпика с таким номером нет!");
        }
    }

    public Epic getEpicById(int id) {
        if (epicStorage.containsKey(id)) {
            System.out.println("Ваш эпик найден!");
            epicStorage.get(id).setStatus(calculateEpicStatus(epicStorage.get(id)));
            return epicStorage.get(id);
        }
        System.out.println("Такого эпика у нас нет!");
        return null;
    }

    public ArrayList<Epic> getAllEpics(){
        if (epicId != 1) {
            for (Epic epic : epicStorage.values()){
                epic.setStatus(calculateEpicStatus(epic));
            }
            return new ArrayList<Epic>(epicStorage.values());
        }
        return null;
    }

    public void removeEpic (int id){
        System.out.println("Эпик: " + epicStorage.get(id).name + " удалена!");
        epicStorage.remove(id);
    }

    public void deleteAllEpics(){
        if (epicId != 1) {
            epicStorage.clear();
            epicId = 1;
            System.out.println("Список эпиков очищен!");
        } else {
            System.out.println("Трекер задач пуст!");
        }
    }

    public TaskStatus calculateEpicStatus(Epic epic){
        ArrayList<Integer> listOfSubtaskIDs = epic.getSubtasksId();
        if (listOfSubtaskIDs.isEmpty()){
            return TaskStatus.NEW;
        }
        int numOfDoneSubtasks = 0;
        int numOfNewSubtask = 0;
        for (int subtaskId : listOfSubtaskIDs){
            if (subtaskStorage.get(subtaskId).getStatus().equals(TaskStatus.DONE)){
                numOfDoneSubtasks++;
            } else if (subtaskStorage.get(subtaskId).getStatus().equals(TaskStatus.NEW)){
                numOfNewSubtask++;
            }
        }
        if (numOfDoneSubtasks == listOfSubtaskIDs.size()){
            return TaskStatus.DONE;
        }
        if (numOfNewSubtask == listOfSubtaskIDs.size()){
            return TaskStatus.NEW;
        }
        return TaskStatus.IN_PROGRESS;
    }

    public ArrayList<Subtask> getEpicsSubtasks(Epic epic){
        ArrayList <Subtask> subtasks = new ArrayList<>();
        if (epic == null){
            return null;
        }
        if (!epic.getSubtasksId().isEmpty()){
            for (int id : epic.getSubtasksId()) {
                subtasks.add(subtaskStorage.get(id));
            }
            return subtasks;
        }
        return null;
    }

    // Beginning of Subtask methods:

    public Subtask createSubtask(Subtask subtask){
        if (subtask == null){
            return null;
        }
        int idOfEpic = subtask.getEpicId();
        if (!epicStorage.containsKey(idOfEpic)){
            System.out.println("Не существует соответствующего Эпика для подзадачи!");
            return null;
        }
        if(subtask.getId() == 0){
            subtask.setId(subtaskId);
        }
        Epic epic = epicStorage.get(idOfEpic);
        epic.addSubtask(subtask.getId());
        subtask.setStatus(TaskStatus.NEW);
        subtaskStorage.put(subtask.getId(), subtask);
        epic.setStatus(calculateEpicStatus(epic));
        subtaskId++;
        return subtask;
    }

    public void updateSubtask(Subtask subtask){
        if (subtask == null){
            return;
        }
        if (!subtaskStorage.containsKey(subtask.getId())){
            return;
        }
        int subtaskEpicId = subtask.getEpicId();
        if (!epicStorage.containsKey(subtaskEpicId)){
            return;
        }
        Epic epic = epicStorage.get(subtaskEpicId);
        ArrayList<Integer> epicSubtaskList = epic.getSubtasksId();
        if (!epicSubtaskList.contains(subtask.getId())){
            return;
        }
        subtaskStorage.put(subtask.getId(), subtask);
        epic.setStatus(calculateEpicStatus(epic));
    }

    public Subtask getSubtaskById (int id){
        if (subtaskStorage.containsKey(id)) {
            System.out.println("Ваша задача найдена!");
            return subtaskStorage.get(id);
        }
        System.out.println("Такой задачи у нас нет!");
        return null;
    }

    public ArrayList<Subtask> getAllSubtasks(){
        if (subtaskId != 1) {
            return new ArrayList<Subtask>(subtaskStorage.values());
        }
        return null;
    }

    public void removeSubtask (int id){
        if (!subtaskStorage.containsKey(id)){
            return;
        }
        int relatedEpicId = subtaskStorage.get(id).getEpicId();
        Epic relatedEpic = epicStorage.get(relatedEpicId);
        relatedEpic.deleteSubtask(id);
        relatedEpic.setStatus(calculateEpicStatus(relatedEpic));
        subtaskStorage.remove(id);
    }

    public void deleteAllSubtasks (){
        if (subtaskStorage.isEmpty()){
            return;
        }
        subtaskStorage.clear();
        for (Epic epic : epicStorage.values()){
            epic.clearSubtasksList();
            epic.setStatus(calculateEpicStatus(epic));
        }
    }

}

