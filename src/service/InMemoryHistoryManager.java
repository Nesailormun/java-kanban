package service;

import module.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final List<Task> taskHistoryStorage = new ArrayList<>(10);

    @Override
    public void add (Task task){
        taskHistoryStorage.add(task);
        controlSizeofTaskHistory();
    }

    @Override
    public List<Task> getHistory(){
        if (taskHistoryStorage.isEmpty()) return new ArrayList<>();
        return taskHistoryStorage;
    }

    private void controlSizeofTaskHistory(){
        if (taskHistoryStorage.size() > 10){
            taskHistoryStorage.removeFirst();
        }
    }
}
