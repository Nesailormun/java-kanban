import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    private int taskId = 1;
    private final HashMap<Integer, Task> taskStorage = new HashMap<>();
    private final HashMap<Integer, Epic> epicStorage = new HashMap<>();
    private final HashMap<Integer, Subtask> subtaskStorage = new HashMap<>();
    private final HistoryManager historyManager = Manager.getDefaultHistory();

    @Override
    public List<Task> getHistory(){
        return historyManager.getHistory();
    }

    // Beginning of Task methods:


    @Override
    public Task getTaskById(int id) {
        if (taskStorage.containsKey(id)) {
            historyManager.add(taskStorage.get(id));
            return taskStorage.get(id);
        }
        return null;
    }

    @Override
    public Task createTask(Task task) {
        if (task == null) {
            return null;
        }
        if (task.getId() == 0) {
            task.setId(taskId);
        }
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.NEW);
        }
        taskStorage.put(task.getId(), task);
        taskId++;
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) {
            return;
        }
        if (taskStorage.containsKey(task.getId())) {
            taskStorage.put(task.getId(), task);
        }
    }

    @Override
    public void removeTask(int id) {
        if (!taskStorage.containsKey(id)) {
            return;
        }
        taskStorage.remove(id);
    }

    @Override
    public void deleteAllTasks() {
        if (!taskStorage.isEmpty()) {
            taskStorage.clear();
        }
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        if (!taskStorage.isEmpty()) {
            return new ArrayList<>(taskStorage.values());
        }
        return null;
    }

    // Beginning of Epic methods:

    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        if ((epic.getId() == 0)) {
            epic.setId(taskId);
        }
        epic.setStatus(calculateEpicStatus(epic));
        epicStorage.put(epic.getId(), epic);
        taskId++;
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        if (epicStorage.containsKey(epic.getId())) {
            epicStorage.get(epic.getId()).setName(epic.getName());
            epicStorage.get(epic.getId()).setDescription(epic.getDescription());
        }
    }

    @Override
    public Epic getEpicById(int id) {
        if (epicStorage.containsKey(id)) {
            historyManager.add(epicStorage.get(id));
            return epicStorage.get(id);
        }
        return null;
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        if (!epicStorage.isEmpty()) {
            return new ArrayList<>(epicStorage.values());
        }
        return null;
    }

    @Override
    public void removeEpic(int id) {
        if (!epicStorage.containsKey(id)) {
            return;
        }
        for (int subtaskId : epicStorage.get(id).getSubtasksId()) {
            subtaskStorage.remove(subtaskId);
        }
        epicStorage.remove(id);
    }

    @Override
    public void deleteAllEpics() {
        if (!epicStorage.isEmpty()) {
            epicStorage.clear();
            subtaskStorage.clear();
        }
    }

    private TaskStatus calculateEpicStatus(Epic epic) {
        ArrayList<Integer> listOfSubtaskIDs = epic.getSubtasksId();
        if (listOfSubtaskIDs.isEmpty()) {
            return TaskStatus.NEW;
        }
        int numOfDoneSubtasks = 0;
        int numOfNewSubtask = 0;
        for (int subtaskId : listOfSubtaskIDs) {
            if (TaskStatus.DONE.equals(subtaskStorage.get(subtaskId).getStatus())) {
                numOfDoneSubtasks++;
            } else if (TaskStatus.NEW.equals(subtaskStorage.get(subtaskId).getStatus())) {
                numOfNewSubtask++;
            }
        }
        if (numOfDoneSubtasks == listOfSubtaskIDs.size()) {
            return TaskStatus.DONE;
        }
        if (numOfNewSubtask == listOfSubtaskIDs.size()) {
            return TaskStatus.NEW;
        }
        return TaskStatus.IN_PROGRESS;
    }

    @Override
    public ArrayList<Subtask> getEpicsSubtasks(Epic epic) {
        ArrayList<Subtask> subtasks = new ArrayList<>();
        if (epic == null) {
            return null;
        }
        if (!epic.getSubtasksId().isEmpty()) {
            for (int id : epic.getSubtasksId()) {
                subtasks.add(subtaskStorage.get(id));
            }
            return subtasks;
        }
        return null;
    }

    // Beginning of Subtask methods:

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask == null) {
            return null;
        }
        int idOfEpic = subtask.getEpicId();
        if (!epicStorage.containsKey(idOfEpic)) {
            return null;
        }
        if (subtask.getId() == 0) {
            subtask.setId(taskId);
        }
        if (subtask.getStatus() == null) {
            subtask.setStatus(TaskStatus.NEW);
        }
        Epic epic = epicStorage.get(idOfEpic);
        epic.addSubtask(subtask.getId());
        subtaskStorage.put(subtask.getId(), subtask);
        epic.setStatus(calculateEpicStatus(epic));
        taskId++;
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return;
        }
        if (!subtaskStorage.containsKey(subtask.getId())) {
            return;
        }
        int subtaskEpicId = subtask.getEpicId();
        if (!epicStorage.containsKey(subtaskEpicId)) {
            return;
        }
        Epic epic = epicStorage.get(subtaskEpicId);
        ArrayList<Integer> epicSubtaskList = epic.getSubtasksId();
        if (!epicSubtaskList.contains(subtask.getId())) {
            return;
        }
        subtaskStorage.put(subtask.getId(), subtask);
        epic.setStatus(calculateEpicStatus(epic));
    }

    @Override
    public Subtask getSubtaskById(int id) {
        if (subtaskStorage.containsKey(id)) {
            historyManager.add(subtaskStorage.get(id));
            return subtaskStorage.get(id);
        }
        return null;
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        if (!subtaskStorage.isEmpty()) {
            return new ArrayList<>(subtaskStorage.values());
        }
        return null;
    }

    @Override
    public void removeSubtask(int id) {
        if (!subtaskStorage.containsKey(id)) {
            return;
        }
        int relatedEpicId = subtaskStorage.get(id).getEpicId();
        Epic relatedEpic = epicStorage.get(relatedEpicId);
        relatedEpic.deleteSubtask(id);
        relatedEpic.setStatus(calculateEpicStatus(relatedEpic));
        subtaskStorage.remove(id);
    }

    @Override
    public void deleteAllSubtasks() {
        if (subtaskStorage.isEmpty()) {
            return;
        }
        subtaskStorage.clear();
        for (Epic epic : epicStorage.values()) {
            epic.clearSubtasksList();
            epic.setStatus(calculateEpicStatus(epic));
        }
    }
}


