import manager.Managers;
import manager.TaskManager;
import tasks.*;


public class Main {

    public static void main(String[] args) {
        TaskManager taskManager;
        taskManager = Managers.getDefault();

        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW);
        taskManager.createTask(task1);

        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        taskManager.createEpic(epic0);
        Epic epic1 = new Epic("Заголовок второго эпика", "Описание второго эпика");
        taskManager.createEpic(epic1);

        SubTask subTask0 = new SubTask("Заголовок первого сабтаска", "Описание первого сабтаска", Status.NEW, 2);
        taskManager.createSubTask(subTask0);
        SubTask subTask1 = new SubTask("Заголовок второго сабтаска", "Описание второго сабтаска", Status.NEW, 2);
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("Заголовок третьего сабтаска", "Описание третьего сабтаска", Status.NEW, 2);
        taskManager.createSubTask(subTask2);

        taskManager.getTaskById(1);
        taskManager.getTaskById(0);
        taskManager.getSubTaskById(6);
        taskManager.getEpicById(3);
        taskManager.getEpicById(2);
        taskManager.getSubTaskById(5);
        taskManager.getSubTaskById(4);

        System.out.println(taskManager.getHistory());

        taskManager.getTaskById(0);
        taskManager.getSubTaskById(4);
        taskManager.getTaskById(1);
        taskManager.getSubTaskById(6);
        taskManager.getEpicById(3);
        taskManager.getEpicById(2);
        taskManager.getSubTaskById(5);

        System.out.println(taskManager.getHistory());
        System.out.println("Удаляем вторую задачу ->" + taskManager.deleteTaskById(1));
        System.out.println(taskManager.getHistory());
        System.out.println("Удаляем эпик с тремя сабтасками ->" + taskManager.deleteEpicById(2));
        System.out.println("По итогу получилось: ");
        System.out.println(taskManager.getHistory());
    }
}