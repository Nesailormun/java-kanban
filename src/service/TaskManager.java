package service;

import exceptions.DateTimeIntersectionException;
import exceptions.NotFoundException;
import exceptions.NullEqualsException;
import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;


public interface TaskManager {

    List<Task> getPrioritizedTasks();

    List<Task> getHistory();
    // Beginning of module.Task methods:

    Task getTaskById(int id) throws NotFoundException;

    Task createTask(Task task) throws NullEqualsException, DateTimeIntersectionException;

    void updateTask(Task task) throws NotFoundException, NullEqualsException, DateTimeIntersectionException;

    void removeTask(int id) throws NotFoundException;

    void deleteAllTasks();

    List<Task> getAllTasks();

    // Beginning of module.Epic methods:

    Epic createEpic(Epic epic) throws NullEqualsException;

    void updateEpic(Epic epic) throws NotFoundException, NullEqualsException;

    Epic getEpicById(int id) throws NotFoundException;

    List<Epic> getAllEpics();

    void removeEpic(int id) throws NotFoundException;

    void deleteAllEpics();

    List<Subtask> getEpicsSubtasks(Epic epic) throws NullEqualsException;

    // Beginning of module.Subtask methods:

    Subtask createSubtask(Subtask subtask) throws NotFoundException, NullEqualsException, DateTimeIntersectionException;

    void updateSubtask(Subtask subtask) throws NotFoundException, NullEqualsException, DateTimeIntersectionException;

    Subtask getSubtaskById(int id) throws NotFoundException;

    List<Subtask> getAllSubtasks();

    void removeSubtask(int id) throws NotFoundException;

    void deleteAllSubtasks();

}

