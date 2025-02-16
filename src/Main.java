import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import tasks.*;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager;
        taskManager = Managers.getDefault();
        Epic epic = new Epic("Придумать много тестов", "Написать хотя бы один тест");
        taskManager.createEpic(epic);
        SubTask subTask = new SubTask("Придумать тест к методу сабтаска",
                "Написать тест к методу createNewSubTask", Status.DONE, epic.getId());
        taskManager.createSubTask(subTask);
        SubTask updatedSubTask = new SubTask("Придумать тест к методам сабтаска",
                "Написать тесты к методам createNewSubTask и updateSubTask",
                Status.IN_PROGRESS, epic.getId());
        updatedSubTask.setId(1);
        taskManager.updateSubTask(updatedSubTask);
        /*TaskManager taskManager = Managers.getDefault();

        System.out.println("Создаём три задачи:");
        Task task0 = new Task("Homework", "do homework", Status.NEW);
        task0 = taskManager.createTask(task0);
        Task task1 = new Task("Classwork", "do classwork", Status.NEW);
        task1 = taskManager.createTask(task1);
        Task task2 = new Task("Chores", "do chores", Status.NEW);
        task2 = taskManager.createTask(task2);

        System.out.println(task0);
        System.out.println(task1);
        System.out.println(task2);

        Task task = task1;
        task.setStatus(Status.IN_PROGRESS);

        task1 = taskManager.updateTask(task);

        System.out.println("Изменили статус у task1: \n" + task1);

        System.out.println("Задание с id = 2: \n" + taskManager.getTaskById(2));

        taskManager.deleteTaskById(1);

        System.out.println("Список всех задач без задачи с id = 1: \n" + taskManager.getAllTasks());

        taskManager.deleteAllTasks();

        System.out.println("Список задач после их полного удаления:");
        System.out.println(taskManager.getAllTasks());

        Epic epic0 = new Epic("First epic", "This epic has three subtasks");
        taskManager.createEpic(epic0);
        SubTask subTask0 = new SubTask("First subtask of first epic", "This is first subtask, do nothing idk", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask0);
        SubTask subTask1 = new SubTask("The second subtask of the first epic", "This is the second subtask, look at me", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("The third subtask of the first epic", "This is the third subtask, the last one", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask2);

        System.out.println("Эпик с id = 3: " + taskManager.getEpicById(3));
        System.out.println("Все сабтаски этого эпика: \n" + taskManager.getAllSubTasksOfOneEpic(epic0.getId()));

        System.out.println("Статус эпика с \"новыми\" сабтасками: \n" + epic0.getStatus());
        subTask2.setStatus(Status.IN_PROGRESS);
        System.out.println("Меняем статус 3го сабтаска: \n" + taskManager.updateSubTask(subTask2) + "\nСтатус эпика в процессе выполнения 3го сабтаска: \n" + epic0.getStatus());

        System.out.println("Создаём ещё один эпик с 4мя сабтаксками:");
        Epic epic1 = new Epic("Second epic", "This epic has four subtasks");
        taskManager.createEpic(epic1);
        SubTask subTask10 = new SubTask("First subtask of the second epic", "This is first subtask, do nothing idk", Status.NEW, epic1.getId());
        taskManager.createSubTask(subTask10);
        SubTask subTask11 = new SubTask("The second subtask of the second epic", "This is the second subtask, look at me", Status.NEW, epic1.getId());
        taskManager.createSubTask(subTask11);
        SubTask subTask12 = new SubTask("The third subtask of the second epic", "This is the third subtask, not the last one", Status.NEW, epic1.getId());
        taskManager.createSubTask(subTask12);
        SubTask subTask13 = new SubTask("The fourth subtask of the second epic", "This is the fourth subtask, the last one", Status.NEW, epic1.getId());
        taskManager.createSubTask(subTask13);

        System.out.println(epic1);
        System.out.println(taskManager.getAllSubTasksOfOneEpic(epic1.getId()));

        System.out.println("Выполняем все сабтаски второго эпика: ");
        subTask10.setStatus(Status.DONE);
        subTask11.setStatus(Status.DONE);
        subTask12.setStatus(Status.DONE);
        subTask13.setStatus(Status.DONE);
        taskManager.updateSubTask(subTask10);
        taskManager.updateSubTask(subTask11);
        taskManager.updateSubTask(subTask12);
        taskManager.updateSubTask(subTask13);
        System.out.println(taskManager.getAllSubTasksOfOneEpic(epic1.getId()));
        System.out.println("Статус эпика: " + epic1.getStatus());

        System.out.println("Выводим все эпики: ");
        System.out.println(taskManager.getAllEpics());
        System.out.println("Выводим все сабтаски: ");
        System.out.println(taskManager.getAllSubTasks());

        System.out.println("Обновляем описания второго эпика и его четвёртого сабтаска");
        subTask13.setDescription("ОПИСАНИЕ ОБНОВЛЕНО");
        epic1.setDescription("ОПИСАНИЕ ОБНОВЛЕНО");
        System.out.println(taskManager.updateSubTask(subTask13));
        System.out.println(taskManager.updateEpic(epic1));
        System.out.println("ИСТОРИЯ");
        System.out.println(taskManager.getHistory());
        System.out.println("ИСТОРИЯ ЗАВЕРШЕНА");
        System.out.println("Удаляем второй сабтаск первого эпика");
        System.out.println(taskManager.deleteSubTaskById(5));
        System.out.println(taskManager.getAllSubTasksOfOneEpic(epic0.getId()));

        System.out.println("Удаляем все сабтаски");
        taskManager.deleteAllSubTasks();
        System.out.println("Эпики: ");
        System.out.println(taskManager.getAllEpics());
        System.out.println("Сабтаски:");
        System.out.println(taskManager.getAllSubTasks());

        System.out.println("Удаляем все эпики");
        taskManager.deleteAllEpics();
        System.out.println("Эпики: ");
        System.out.println(taskManager.getAllEpics());
*/


    }
}