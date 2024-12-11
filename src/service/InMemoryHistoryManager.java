package service;

import module.CustomLinkedHashMap;
import module.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    CustomLinkedHashMap history = new CustomLinkedHashMap();

    @Override
    public void add(Task task) {
        if (!history.getStorage().containsKey(task.getId())) {
            history.linkLast(task);
        } else {
            history.removeNode(history.getStorage().get(task.getId()));
            history.linkLast(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        if (history.getStorage().isEmpty()) return new ArrayList<>();
        return history.getTasks();
    }

    @Override
    public void remove(int id) {
        if (!history.getStorage().isEmpty()){
            if (history.getStorage().containsKey(id)) {
                history.removeNode(history.getStorage().get(id));
                history.getStorage().remove(id);
            }
        }
    }

}