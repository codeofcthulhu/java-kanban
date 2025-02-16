package manager;

import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager{
    private Map<Integer, Node> history;
    private Node head;
    private Node tail;


    private static class Node {
        private Node next;
        private Node previous;
        private Task task;

        public Node() {
        }

        public Node(Node next, Node previous, Task task) {
            this.next = next;
            this.previous = previous;
            this.task = task;
        }
    }

    public InMemoryHistoryManager() {
        history = new HashMap<>();
    }

    public void linkLast(Task task) {
        Node node = new Node(null, tail, task);
        tail.next = node;
        tail = node;
    }

    private void removeNode(Node node) {
        Node nodePrevious = node.previous;
        Node nodeNext = node.next;
        if (node == head) {
            head = nodeNext;
            nodeNext.previous = null;
        } else if (node == tail) {
            tail = nodePrevious;
            nodePrevious.next = null;
        } else {
            nodePrevious.next = nodeNext;
            nodeNext.previous = nodePrevious;
        }
    }

    private List<Task> getTasks() {
        Node node = head;
        ArrayList<Task> historyList = new ArrayList<>(history.size());
        while (node != null) {
            historyList.add(node.task);
            node = node.next;
        }
        return historyList;
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        } else if (history.isEmpty()) {
            Node node = new Node(null, null, task);
            head = node;
            tail = node;
            history.put(task.getId(), node);
        } else if (history.containsKey(task.getId())) {
            Node node = history.get(task.getId());
            removeNode(node);
            linkLast(task);
            history.put(task.getId(), tail);
        } else {
            linkLast(task);
            history.put(task.getId(), tail);
        }

    }

    @Override
    public void remove(int id) {
        removeNode(history.get(id));
        history.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        ArrayList<Task> listToReturn = new ArrayList<>(history.size());
        List<Task> historyList = getTasks();
        for (Task task : historyList) {
            listToReturn.add(new Task(task));
        }
        return listToReturn;
    }
}
