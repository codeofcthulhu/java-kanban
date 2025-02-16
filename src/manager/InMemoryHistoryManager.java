package manager;

import tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
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

    private void linkLast(Task task) {
        Node node;
        if (isNodesEmpty()) {
            node = new Node(null, null, task);
            head = node;
        } else {
            node = new Node(null, tail, task);
            tail.next = node;
        }
        tail = node;
    }

    private void removeNode(Node node) {
        if ((node == head) && (node == tail)) {
            head = null;
            tail = null;
        } else if (node == head) {
            Node nodeNext = node.next;
            head = nodeNext;
            nodeNext.previous = null;
        } else if (node == tail) {
            Node nodePrevious = node.previous;
            tail = nodePrevious;
            nodePrevious.next = null;
        } else {
            Node nodeNext = node.next;
            Node nodePrevious = node.previous;
            nodePrevious.next = nodeNext;
            nodeNext.previous = nodePrevious;
        }
    }

    private List<Task> getTasks() {
        if (isNodesEmpty()) {
            return null;
        }
        Node node = head;
        ArrayList<Task> historyList = new ArrayList<>(history.size());
        while (node != null) {
            historyList.add(node.task);
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
        if (history.containsKey(id)) {
            removeNode(history.get(id));
            history.remove(id);
        }
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
