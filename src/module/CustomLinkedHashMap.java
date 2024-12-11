package module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomLinkedHashMap {
    private Node head;
    private Node tail;
    private final Map<Integer, Node> storage = new HashMap<>();

    public Map<Integer, Node> getStorage() {
        return storage;
    }

    public void linkLast(Task task) {
        final Node oldTail = tail;
        final Node newTail = new Node(oldTail, task, null);
        tail = newTail;
        if (oldTail == null) {
            head = newTail;
        } else {
            oldTail.setNext(newTail);
        }
        storage.put(task.getId(), newTail);

    }

    public List<Task> getTasks() {
        final List<Task> tasks = new ArrayList<>();
        for (Node current = head; current != null; current = current.getNext()) {
            tasks.add(current.getTask());
        }
        return tasks;
    }

    public void removeNode(Node node){
        if (node == null) return;
        for (Node elem = head; elem != null; elem = elem.getNext()){
            if (node.getTask().equals(elem.getTask())){
                final Node next = elem.getNext();
                final Node prev = elem.getPrev();
                if (prev == null) {
                    head = next;
                } else {
                    prev.setNext(next);
                    elem.setPrev(null);
                }

                if (next == null) {
                    tail = prev;
                } else {
                    next.setPrev(prev);
                    elem.setNext(null);
                }
                elem.setTask(null);
            }
        }
    }
}
