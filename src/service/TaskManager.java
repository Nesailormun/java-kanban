package service;

import module.Epic;
import module.Subtask;
import module.Task;

import java.util.ArrayList;
import java.util.List;


public interface TaskManager {

    List<Task> getHistory();
    // Beginning of module.Task methods:

    Task getTaskById(int id);


    Task createTask(Task task);


    void updateTask(Task task);

    void removeTask(int id);

    void deleteAllTasks();

    List<Task> getAllTasks();


    // Beginning of module.Epic methods:

    Epic createEpic(Epic epic);

    void updateEpic(Epic epic);

    Epic getEpicById(int id);

    List<Epic> getAllEpics();

    void removeEpic(int id);

    void deleteAllEpics();


    List<Subtask> getEpicsSubtasks(Epic epic);


    // Beginning of module.Subtask methods:

    Subtask createSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    Subtask getSubtaskById(int id);

    List<Subtask> getAllSubtasks();

    void removeSubtask(int id);

    void deleteAllSubtasks();
}

