package manager;

import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private Map<Integer, Node<Task>> history;
    private Node<Task> head;
    private Node<Task> tail;


    private static class Node<T> {
        private Node<T> next;
        private Node<T> previous;
        private T t;

        public Node() {
        }

        public Node(Node<T> next, Node<T> previous, T t) {
            this.next = next;
            this.previous = previous;
            this.t = t;
        }
    }

    public InMemoryHistoryManager() {
        history = new HashMap<>();
    }

    private void linkLast(Task task) {
        Node<Task> node;
        if (isNodesEmpty()) {
            node = new Node<>(null, null, task);
            head = node;
        } else {
            node = new Node<>(null, tail, task);
            tail.next = node;
        }
        tail = node;
    }

    private void removeNode(Node<Task> node) {
        if ((node == head) && (node == tail)) {
            head = null;
            tail = null;
        } else if (node == head) {
            Node<Task> nodeNext = node.next;
            head = nodeNext;
            nodeNext.previous = null;
        } else if (node == tail) {
            Node<Task> nodePrevious = node.previous;
            tail = nodePrevious;
            nodePrevious.next = null;
        } else {
            Node<Task> nodeNext = node.next;
            Node<Task> nodePrevious = node.previous;
            nodePrevious.next = nodeNext;
            nodeNext.previous = nodePrevious;
        }
    }

    private List<Task> getTasks() {
        if (isNodesEmpty()) {
            return null;
        }
        Node<Task> node = head;
        ArrayList<Task> historyList = new ArrayList<>(history.size());
        while (node != null) {
            historyList.add(node.t);
            node = node.next;
        }
        return historyList;
    }

    private boolean isNodesEmpty() {
        return (head == null) && (tail == null);
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        } else if (history.isEmpty()) {
            Node<Task> node = new Node<>(null, null, task);
            head = node;
            tail = node;
            history.put(task.getId(), node);
        } else if (history.containsKey(task.getId())) {
            Node<Task> node = history.get(task.getId());
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
        if (history.containsKey(id)) {
            removeNode(history.get(id));
            history.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        ArrayList<Task> listToReturn = new ArrayList<>(history.size());
        List<Task> historyList = getTasks();
        if (historyList == null) {
            return null;
        }
        for (Task task : historyList) {
            listToReturn.add(new Task(task));
        }
        return listToReturn;
    }
}
