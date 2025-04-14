package manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import exceptions.TaskOverlapException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.SubTask;
import tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    public void init() {
        taskManager = createTaskManager();
    }

    @Test
    void shouldReturnTrueTwoTasksAreEqualWhenItsIdsAreEqual() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        Task task1 = new Task("Иной заголовок", "Иное описание", Status.DONE);

        taskManager.createTask(task0);
        task1.setId(0);

        assertTrue(task0.equals(task1));
    }

    @Test
    void shouldReturnTrueTwoObjectsOfSubClassesOfTaskClassAreEqualWhenItsIdsAreEqual() {
        Epic epic = new Epic("Загловок эпика", "Описание эпика");
        SubTask subTask = new SubTask("Загловок сабтаска", "Описание сабтаска", Status.NEW, 0);

        epic.setId(0);
        subTask.setId(0);

        assertTrue(epic.equals(subTask));
    }

    @Test
    void managersClassMethodsShouldCreateReadyToUseInstancesOfManagers() {
        assertTrue(taskManager != null);
    }

    @Test
    void inMemoryTaskManagerCanCreateDifferentTasksAndCanFindThemById() {
        Task task = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        Epic epic = new Epic("Загловок эпика", "Описание эпика");
        SubTask subTask = new SubTask("Загловок сабтаска", "Описание сабтаска", Status.NEW, 1);
        taskManager.createTask(task);
        taskManager.createEpic(epic);
        taskManager.createSubTask(subTask);

        Task resultById0 = taskManager.getTaskById(0);
        Task resultById1 = taskManager.getEpicById(1);
        Task resultById2 = taskManager.getSubTaskById(2);

        assertTrue(resultById0 instanceof Task);
        assertTrue(resultById1 instanceof Epic);
        assertTrue(resultById2 instanceof SubTask);
        assertEquals(0, resultById0.getId());
        assertEquals(1, resultById1.getId());
        assertEquals(2, resultById2.getId());

    }

    @Test
    void taskWithGeneratedInManagerIdAndTaskWithSettedIdDoesNotConflictInsideManager() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        Task task1 = new Task("Иной заголовок", "Иное описание", Status.DONE);

        taskManager.createTask(task0);
        task1.setId(0);
        taskManager.createTask(task1);

        assertTrue(task0.getId() != task1.getId());
    }

    @Test
    void taskShouldNotBeChangedAfterAddingToManager() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);

        Task addedTask = taskManager.createTask(task0);

        assertEquals(addedTask.getName(), task0.getName());
        assertEquals(addedTask.getDescription(), task0.getDescription());
        assertEquals(addedTask.getStatus(), task0.getStatus());
    }

    @Test
    void shouldCreateTaskWithZeroId() {
        Task task = new Task("Придумать много тестов", "Написать хотя бы один тест", Status.NEW);
        Task taskResult = taskManager.createTask(task);
        assertEquals(0, taskResult.getId());
        assertEquals("Придумать много тестов", taskResult.getName());
        assertEquals("Написать хотя бы один тест", taskResult.getDescription());
        assertEquals(Status.NEW, taskResult.getStatus());
    }

    @Test
    void statusDescriptionAndNameShouldBeuUpdated() {
        Task task = new Task("Придумать много тестов", "Написать хотя бы один тест", Status.NEW);
        taskManager.createTask(task);
        Task updatedTask = new Task("Придумать достаточно много тестов", "Написать хотя бы три теста", Status.IN_PROGRESS);
        updatedTask.setId(task.getId());

        Task taskResult = taskManager.updateTask(updatedTask);

        assertEquals(0, taskResult.getId());
        assertEquals("Придумать достаточно много тестов", taskResult.getName());
        assertEquals("Написать хотя бы три теста", taskResult.getDescription());
        assertEquals(Status.IN_PROGRESS, taskResult.getStatus());
    }

    @Test
    void FirstAndThirdTasksShouldBeRemovedByIdTheSecondShouldStay() {
        Task task0 = new Task("Придумать много тестов", "Написать хотя бы один тест", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Придумать ещё больше тестов", "Написать хотя бы один тест", Status.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Придумать максимум тестов", "Написать хотя бы один тест", Status.NEW);
        taskManager.createTask(task2);

        taskManager.deleteTaskById(0);
        taskManager.deleteTaskById(2);

        Assertions.assertNull(taskManager.getTaskById(0));
        Assertions.assertNotNull(taskManager.getTaskById(1));
        Assertions.assertNull(taskManager.getTaskById(2));
    }

    @Test
    void shouldReturnFourCreatedTasks() {
        Task task0 = new Task("Придумать много тестов", "Написать хотя бы один тест", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Придумать ещё больше тестов", "Написать хотя бы один тест", Status.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Придумать максимум тестов", "Написать хотя бы один тест", Status.IN_PROGRESS);
        taskManager.createTask(task2);
        Task task3 = new Task("Сделать тесты по тз", "Написать хотя бы пару тестов", Status.NEW);
        taskManager.createTask(task3);

        ArrayList<Task> resultList = new ArrayList<>(taskManager.getAllTasks());

        assertEquals(0, resultList.get(0).getId());
        assertEquals("Придумать много тестов", resultList.get(0).getName());
        assertEquals("Написать хотя бы один тест", resultList.get(0).getDescription());
        assertEquals(Status.NEW, resultList.get(0).getStatus());
        assertEquals(1, resultList.get(1).getId());
        assertEquals("Придумать ещё больше тестов", resultList.get(1).getName());
        assertEquals("Написать хотя бы один тест", resultList.get(1).getDescription());
        assertEquals(Status.NEW, resultList.get(1).getStatus());
        assertEquals(2, resultList.get(2).getId());
        assertEquals("Придумать максимум тестов", resultList.get(2).getName());
        assertEquals("Написать хотя бы один тест", resultList.get(2).getDescription());
        assertEquals(Status.IN_PROGRESS, resultList.get(2).getStatus());
        assertEquals(3, resultList.get(3).getId());
        assertEquals("Сделать тесты по тз", resultList.get(3).getName());
        assertEquals("Написать хотя бы пару тестов", resultList.get(3).getDescription());
        assertEquals(Status.NEW, resultList.get(3).getStatus());
    }

    @Test
    void theSecondTaskShouldBeReturnAfterGetByIdCall() {
        Task task0 = new Task("Придумать много тестов", "Написать хотя бы один тест", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Придумать ещё больше тестов", "Написать хотя бы один тест", Status.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Придумать максимум тестов", "Написать хотя бы один тест", Status.NEW);
        taskManager.createTask(task2);

        Task resultTask = taskManager.getTaskById(1);

        assertEquals(task1, resultTask);
    }

    @Test
    void getAllTasksShouldReturnNull() {
        Task task0 = new Task("Тесты для тасков", "Написать хотя бы один тест для тасков", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Тесты для сабтасков", "Написать хотя бы один тест для сабтасков", Status.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Тесты для эпиков", "Написать хотя бы один тест для эпиков", Status.NEW);
        taskManager.createTask(task2);
        Task task3 = new Task("Тесты для менеджера", "Написать хотя бы один тест для менеджера", Status.NEW);
        taskManager.createTask(task3);

        taskManager.deleteAllTasks();

        assertEquals(new ArrayList<>(), taskManager.getAllTasks());
    }

    @Test
    void shouldCreateEpicWithZeroIdAndItsSubTaskWithIdEqualsOne() {
        Epic epic = new Epic("Придумать много тестов", "Написать хотя бы один тест");
        Epic epicResult = taskManager.createEpic(epic);
        SubTask subTask = new SubTask("Придумать тест к методу сабтаска",
                "Написать тест к методу createNewSubTask", Status.NEW, epic.getId());
        ArrayList<Integer> expectedListOfSubTaskIds = new ArrayList<>();
        expectedListOfSubTaskIds.add(1);

        SubTask subTaskResult = taskManager.createSubTask(subTask);

        assertEquals("Придумать много тестов", epicResult.getName());
        assertEquals("Написать хотя бы один тест", epicResult.getDescription());
        assertEquals(Status.NEW, epicResult.getStatus());
        assertEquals(0, epicResult.getId());
        assertEquals(expectedListOfSubTaskIds, epicResult.getSubTasksIds());
        assertEquals("Придумать тест к методу сабтаска", subTaskResult.getName());
        assertEquals("Написать тест к методу createNewSubTask", subTaskResult.getDescription());
        assertEquals(Status.NEW, subTaskResult.getStatus());
        assertEquals(1, subTaskResult.getId());
        assertEquals(0, subTaskResult.getEpicId());
    }


    @Test
    void statusDescriptionAndNameOfSubTaskShouldBeuUpdatedEpicsStatusShouldBeUpdatedToo() {
        Epic epic = new Epic("Придумать много тестов", "Написать хотя бы один тест");
        taskManager.createEpic(epic);
        SubTask subTask = new SubTask("Придумать тест к методу сабтаска",
                "Написать тест к методу createNewSubTask", Status.NEW, epic.getId());
        taskManager.createSubTask(subTask);
        SubTask updatedSubTask = new SubTask("Придумать тест к методам сабтаска",
                "Написать тесты к методам createNewSubTask и updateSubTask",
                Status.IN_PROGRESS, epic.getId());
        updatedSubTask.setId(1);
        ArrayList<Integer> expectedListOfSubTaskIds = new ArrayList<>();
        expectedListOfSubTaskIds.add(1);

        SubTask subTaskResult = taskManager.updateSubTask(updatedSubTask);

        assertEquals("Придумать много тестов", taskManager.getEpicById(0).getName());
        assertEquals("Написать хотя бы один тест", taskManager.getEpicById(0).getDescription());
        assertEquals(Status.IN_PROGRESS, taskManager.getEpicById(0).getStatus());
        assertEquals(0, taskManager.getEpicById(0).getId());
        assertEquals(expectedListOfSubTaskIds, taskManager.getEpicById(0).getSubTasksIds());
        assertEquals("Придумать тест к методам сабтаска", subTaskResult.getName());
        assertEquals("Написать тесты к методам createNewSubTask и updateSubTask", subTaskResult.getDescription());
        assertEquals(Status.IN_PROGRESS, subTaskResult.getStatus());
        assertEquals(1, subTaskResult.getId());
        assertEquals(0, subTaskResult.getEpicId());
    }

    @Test
    void subTaskShouldBeDeletedByIdAlsoIdsofSubtasksOfEpicShouldBeNull() {
        Epic epic = new Epic("Придумать много тестов", "Написать хотя бы один тест");
        Epic epicResult = taskManager.createEpic(epic);
        SubTask subTask = new SubTask("Придумать тест к методу сабтаска",
                "Написать тест к методу createNewSubTask", Status.NEW, epic.getId());
        taskManager.createSubTask(subTask);

        taskManager.deleteSubTaskById(1);

        Assertions.assertNull(taskManager.getTaskById(1));
        assertEquals(new ArrayList<>(), taskManager.getAllSubTasksOfOneEpic(0));
    }

    @Test
    void allSubTasksOfThreeEpicsShouldBeReturned() {
        Epic epic0 = new Epic("Придумать много тестов", "Написать хотя бы один тест");
        taskManager.createEpic(epic0);
        SubTask subTask0 = new SubTask("Придумать тест к методам таска",
                "Написать тест к методу createTask", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask0);
        SubTask subTask1 = new SubTask("Придумать тест к методу сабтаска",
                "Написать тест к методу createSubTask", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("Придумать тест к методу эпика",
                "Написать тест к методу createEpic", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask2);
        Epic epic1 = new Epic("Придумать много тестов для объектов пакета менеджера",
                "Написать хотя бы один тест");
        taskManager.createEpic(epic1);
        SubTask subTask3 = new SubTask("Придумать тест к методу InMemoryTaskManager",
                "Написать тест к методу", Status.NEW, epic1.getId());
        taskManager.createSubTask(subTask3);
        SubTask subTask4 = new SubTask("Придумать тест к методу HistoryManager",
                "Написать тест к методу", Status.NEW, epic1.getId());
        taskManager.createSubTask(subTask4);
        Epic epic2 = new Epic("Отдохнуть от Unit тестов", "Делать что угодно кроме тестов");
        taskManager.createEpic(epic2);
        SubTask subTask5 = new SubTask("Единение с природой",
                "Пойти потрогать траву, поглядеть на небо", Status.NEW, epic2.getId());
        taskManager.createSubTask(subTask5);

        List<SubTask> resultList = taskManager.getAllSubTasks();

        assertEquals("Придумать тест к методам таска", resultList.get(0).getName());
        assertEquals("Написать тест к методу createTask", resultList.get(0).getDescription());
        assertEquals(Status.NEW, resultList.get(0).getStatus());
        assertEquals(1, resultList.get(0).getId());
        assertEquals(0, resultList.get(0).getEpicId());
        assertEquals("Придумать тест к методу сабтаска", resultList.get(1).getName());
        assertEquals("Написать тест к методу createSubTask", resultList.get(1).getDescription());
        assertEquals(Status.NEW, resultList.get(1).getStatus());
        assertEquals(2, resultList.get(1).getId());
        assertEquals(0, resultList.get(1).getEpicId());
        assertEquals("Придумать тест к методу эпика", resultList.get(2).getName());
        assertEquals("Написать тест к методу createEpic", resultList.get(2).getDescription());
        assertEquals(Status.NEW, resultList.get(2).getStatus());
        assertEquals(3, resultList.get(2).getId());
        assertEquals(0, resultList.get(2).getEpicId());
        assertEquals("Придумать тест к методу InMemoryTaskManager", resultList.get(3).getName());
        assertEquals("Написать тест к методу", resultList.get(3).getDescription());
        assertEquals(Status.NEW, resultList.get(3).getStatus());
        assertEquals(5, resultList.get(3).getId());
        assertEquals(4, resultList.get(3).getEpicId());
        assertEquals("Придумать тест к методу HistoryManager", resultList.get(4).getName());
        assertEquals("Написать тест к методу", resultList.get(4).getDescription());
        assertEquals(Status.NEW, resultList.get(4).getStatus());
        assertEquals(6, resultList.get(4).getId());
        assertEquals(4, resultList.get(4).getEpicId());
        assertEquals("Единение с природой", resultList.get(5).getName());
        assertEquals("Пойти потрогать траву, поглядеть на небо", resultList.get(5).getDescription());
        assertEquals(Status.NEW, resultList.get(5).getStatus());
        assertEquals(8, resultList.get(5).getId());
        assertEquals(7, resultList.get(5).getEpicId());
    }

    @Test
    void subTaskWithIdThreeShouldBeReturned() {
        Epic epic0 = new Epic("Придумать много тестов", "Написать хотя бы один тест");
        taskManager.createEpic(epic0);
        SubTask subTask0 = new SubTask("Придумать тест к методам таска",
                "Написать тест к методу createTask", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask0);
        SubTask subTask1 = new SubTask("Придумать тест к методу сабтаска",
                "Написать тест к методу createSubTask", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("Придумать тест к методу эпика",
                "Написать тест к методу createEpic", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask2);

        SubTask subTaskResult = taskManager.getSubTaskById(3);

        assertEquals("Придумать тест к методу эпика", subTaskResult.getName());
        assertEquals("Написать тест к методу createEpic", subTaskResult.getDescription());
        assertEquals(Status.NEW, subTaskResult.getStatus());
        assertEquals(0, subTaskResult.getEpicId());
        assertEquals(3, subTaskResult.getId());

    }

    @Test
    void allSubTasksOfThreeEpicsShouldBeDeleted() {
        Epic epic0 = new Epic("Придумать много тестов", "Написать хотя бы один тест");
        taskManager.createEpic(epic0);
        SubTask subTask0 = new SubTask("Придумать тест к методам таска",
                "Написать тест к методу createTask", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask0);
        SubTask subTask1 = new SubTask("Придумать тест к методу сабтаска",
                "Написать тест к методу createSubTask", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("Придумать тест к методу эпика",
                "Написать тест к методу createEpic", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask2);
        Epic epic1 = new Epic("Придумать много тестов для объектов пакета менеджера",
                "Написать хотя бы один тест");
        taskManager.createEpic(epic1);
        SubTask subTask3 = new SubTask("Придумать тест к методу InMemoryTaskManager",
                "Написать тест к методу", Status.NEW, epic1.getId());
        taskManager.createSubTask(subTask3);
        SubTask subTask4 = new SubTask("Придумать тест к методу HistoryManager",
                "Написать тест к методу", Status.NEW, epic1.getId());
        taskManager.createSubTask(subTask4);
        Epic epic2 = new Epic("Отдохнуть от Unit тестов", "Делать что угодно кроме тестов");
        taskManager.createEpic(epic2);
        SubTask subTask5 = new SubTask("Единение с природой",
                "Пойти потрогать траву, поглядеть на небо", Status.NEW, epic2.getId());
        taskManager.createSubTask(subTask5);

        taskManager.deleteAllSubTasks();

        assertEquals(new ArrayList<>(), taskManager.getAllSubTasks());
    }

    @Test
    void epicWithNoSubTasksAndEpicWithOneSubTaskShouldBeCreated() {
        Epic epic0 = new Epic("Придумать много тестов", "Написать хотя бы один тест");
        Epic epicResult0 = taskManager.createEpic(epic0);
        Epic epic1 = new Epic("Придумать много тестов для объектов пакета менеджера",
                "Написать хотя бы один тест");
        Epic epicResult1 = taskManager.createEpic(epic1);
        SubTask subTask = new SubTask("Придумать тест к методу InMemoryTaskManager",
                "Написать тест к методу", Status.NEW, epic1.getId());
        taskManager.createSubTask(subTask);
        ArrayList<Integer> resultListForEpic2 = new ArrayList<>();
        resultListForEpic2.add(subTask.getId());

        assertEquals("Придумать много тестов", epicResult0.getName());
        assertEquals("Написать хотя бы один тест", epicResult0.getDescription());
        assertEquals(0, epicResult0.getId());
        assertEquals(new ArrayList<>(), epicResult0.getSubTasksIds());
        assertEquals("Придумать много тестов для объектов пакета менеджера", epicResult1.getName());
        assertEquals("Написать хотя бы один тест", epicResult1.getDescription());
        assertEquals(1, epicResult1.getId());
        assertEquals(resultListForEpic2, epicResult1.getSubTasksIds());


    }

    @Test
    void nameAndDescrioptionOfEpicShouldBeUpdated() {
        Epic epic0 = new Epic("Придумать много тестов", "Написать хотя бы один тест");
        Epic epicUpdated = taskManager.createEpic(epic0);
        epicUpdated.setName("Придумать много различных тестов");
        epicUpdated.setDescription("Написать много много тестов");

        Epic epicResult = taskManager.updateEpic(epicUpdated);

        assertEquals("Придумать много различных тестов", epicResult.getName());
        assertEquals("Написать много много тестов", epicResult.getDescription());


    }

    @Test
    void epicWithIdZeroShouldBeDeleted() {
        Epic epic0 = new Epic("Придумать много тестов", "Написать хотя бы один тест");
        Epic epicResult0 = taskManager.createEpic(epic0);
        Epic epic1 = new Epic("Придумать много тестов для объектов пакета менеджера",
                "Написать хотя бы один тест");
        Epic epicResult1 = taskManager.createEpic(epic1);

        taskManager.deleteEpicById(0);

        Assertions.assertNull(taskManager.getEpicById(0));
    }

    @Test
    void shouldReturnListOfThreeEpics() {
        Epic epic0 = new Epic("Придумать много тестов", "Написать хотя бы один тест");
        taskManager.createEpic(epic0);
        Epic epic1 = new Epic("Придумать много тестов для объектов пакета менеджера",
                "Написать хотя бы один тест");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Отдохнуть от Unit тестов", "Делать что угодно кроме тестов");
        taskManager.createEpic(epic2);

        List<Epic> epics = taskManager.getAllEpics();

        assertEquals("Придумать много тестов", epics.get(0).getName());
        assertEquals("Написать хотя бы один тест", epics.get(0).getDescription());
        assertEquals(0, epics.get(0).getId());
        assertEquals(new ArrayList<>(), epics.get(0).getSubTasksIds());
        assertEquals("Придумать много тестов для объектов пакета менеджера", epics.get(1).getName());
        assertEquals("Написать хотя бы один тест", epics.get(1).getDescription());
        assertEquals(1, epics.get(1).getId());
        assertEquals(new ArrayList<>(), epics.get(1).getSubTasksIds());
        assertEquals("Отдохнуть от Unit тестов", epics.get(2).getName());
        assertEquals("Делать что угодно кроме тестов", epics.get(2).getDescription());
        assertEquals(2, epics.get(2).getId());
        assertEquals(new ArrayList<>(), epics.get(2).getSubTasksIds());

    }

    @Test
    void shouldReturnEpicWithIdFive() {
        Epic epic0 = new Epic("Придумать много тестов", "Написать хотя бы один тест");
        taskManager.createEpic(epic0);
        SubTask subTask0 = new SubTask("Придумать тест к методам таска",
                "Написать тест к методу createTask", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask0);
        SubTask subTask1 = new SubTask("Придумать тест к методу сабтаска",
                "Написать тест к методу createSubTask", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("Придумать тест к методу эпика",
                "Написать тест к методу createEpic", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask2);
        Epic epic1 = new Epic("Придумать много тестов для объектов пакета менеджера",
                "Написать хотя бы один тест");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Отдохнуть от Unit тестов", "Делать что угодно кроме тестов");
        taskManager.createEpic(epic2);

        Epic resultEpic = taskManager.getEpicById(5);

        assertEquals("Отдохнуть от Unit тестов", resultEpic.getName());
        assertEquals("Делать что угодно кроме тестов", resultEpic.getDescription());
        assertEquals(5, resultEpic.getId());
        assertEquals(new ArrayList<>(), resultEpic.getSubTasksIds());
    }

    @Test
    void shouldDeleteAllEpics() {
        Epic epic0 = new Epic("Придумать много тестов", "Написать хотя бы один тест");
        taskManager.createEpic(epic0);
        SubTask subTask0 = new SubTask("Придумать тест к методам таска",
                "Написать тест к методу createTask", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask0);
        SubTask subTask1 = new SubTask("Придумать тест к методу сабтаска",
                "Написать тест к методу createSubTask", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("Придумать тест к методу эпика",
                "Написать тест к методу createEpic", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask2);
        Epic epic1 = new Epic("Придумать много тестов для объектов пакета менеджера",
                "Написать хотя бы один тест");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Отдохнуть от Unit тестов", "Делать что угодно кроме тестов");
        taskManager.createEpic(epic2);

        taskManager.deleteAllEpics();

        assertEquals(new ArrayList<>(), taskManager.getAllEpics());
    }

    @Test
    void shouldReturnAllSubtasksOfEpicWithZeroId() {
        Epic epic0 = new Epic("Придумать много тестов", "Написать хотя бы один тест");
        taskManager.createEpic(epic0);
        SubTask subTask0 = new SubTask("Придумать тест к методам таска",
                "Написать тест к методу createTask", Status.NEW, epic0.getId());
        taskManager.createSubTask(subTask0);
        SubTask subTask1 = new SubTask("Придумать тест к методу сабтаска",
                "Написать тест к методу createSubTask", Status.IN_PROGRESS, epic0.getId());
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("Придумать тест к методу эпика",
                "Написать тест к методу createEpic", Status.DONE, epic0.getId());
        taskManager.createSubTask(subTask2);
        Epic epic1 = new Epic("Придумать много тестов для объектов пакета менеджера",
                "Написать хотя бы один тест");
        SubTask subTask = new SubTask("Придумать тест к методу InMemoryTaskManager",
                "Написать тест к методу", Status.NEW, epic1.getId());
        taskManager.createSubTask(subTask);

        List<SubTask> resultList = taskManager.getAllSubTasksOfOneEpic(0);

        assertEquals("Придумать тест к методам таска", resultList.get(0).getName());
        assertEquals("Написать тест к методу createTask", resultList.get(0).getDescription());
        assertEquals(Status.NEW, resultList.get(0).getStatus());
        assertEquals(1, resultList.get(0).getId());
        assertEquals(0, resultList.get(0).getEpicId());
        assertEquals("Придумать тест к методу сабтаска", resultList.get(1).getName());
        assertEquals("Написать тест к методу createSubTask", resultList.get(1).getDescription());
        assertEquals(Status.IN_PROGRESS, resultList.get(1).getStatus());
        assertEquals(2, resultList.get(1).getId());
        assertEquals(0, resultList.get(1).getEpicId());
        assertEquals("Придумать тест к методу эпика", resultList.get(2).getName());
        assertEquals("Написать тест к методу createEpic", resultList.get(2).getDescription());
        assertEquals(Status.DONE, resultList.get(2).getStatus());
        assertEquals(3, resultList.get(2).getId());
        assertEquals(0, resultList.get(2).getEpicId());
    }

    @Test
    void shouldReturnHistoryWithThirteenTasks() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Заголовок третьего таска", "Описание третьего таска", Status.NEW);
        taskManager.createTask(task2);
        Task task3 = new Task("Заголовок четвёртого таска", "Описание четвёртого таска", Status.NEW);
        taskManager.createTask(task3);
        Task task4 = new Task("Заголовок пятого таска", "Описание пятого таска", Status.NEW);
        taskManager.createTask(task4);
        Task task5 = new Task("Заголовок шестого таска", "Описание шестого таска", Status.NEW);
        taskManager.createTask(task5);
        Task task6 = new Task("Заголовок седьмого таска", "Описание седьмого таска", Status.NEW);
        taskManager.createTask(task6);
        Task task7 = new Task("Заголовок восьмого таска", "Описание восьмого таска", Status.NEW);
        taskManager.createTask(task7);
        Task task8 = new Task("Заголовок девятого таска", "Описание девятого таска", Status.NEW);
        taskManager.createTask(task8);
        Task task9 = new Task("Заголовок десятого таска", "Описание десятого таска", Status.NEW);
        taskManager.createTask(task9);
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        taskManager.createEpic(epic0);
        Epic epic1 = new Epic("Заголовок второго эпика", "Описание второго эпика");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Заголовок третьего эпика", "Описание третьего эпика");
        taskManager.createEpic(epic2);
        taskManager.getEpicById(10);
        taskManager.getEpicById(11);
        taskManager.getEpicById(12);
        taskManager.getTaskById(0);
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(3);
        taskManager.getTaskById(4);
        taskManager.getTaskById(5);
        taskManager.getTaskById(6);
        taskManager.getTaskById(7);
        taskManager.getTaskById(8);
        taskManager.getTaskById(9);

        List<Task> historyList = taskManager.getHistory();

        assertEquals(10, historyList.get(0).getId());
        assertEquals(11, historyList.get(1).getId());
        assertEquals(12, historyList.get(2).getId());
        assertEquals(0, historyList.get(3).getId());
        assertEquals(1, historyList.get(4).getId());
        assertEquals(2, historyList.get(5).getId());
        assertEquals(3, historyList.get(6).getId());
        assertEquals(4, historyList.get(7).getId());
        assertEquals(5, historyList.get(8).getId());
        assertEquals(6, historyList.get(9).getId());
        assertEquals(7, historyList.get(10).getId());
        assertEquals(8, historyList.get(11).getId());
        assertEquals(9, historyList.get(12).getId());
    }

    @Test
    void epicStatusShouldBeEqualNew() {
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        SubTask subTask0 = new SubTask("Заголовок первого сабтаска", "Описание первого сабтаска", Status.NEW, 0);
        SubTask subTask1 = new SubTask("Заголовок второго сабтаска", "Описание второго сабтаска", Status.NEW, 0);
        SubTask subTask2 = new SubTask("Заголовок третьего сабтаска", "Описание третьего сабтаска", Status.NEW, 0);
        SubTask subTask3 = new SubTask("Заголовок четвёртого сабтаска", "Описание четвёртого сабтаска", Status.NEW, 0);
        SubTask subTask4 = new SubTask("Заголовок пятого сабтаска", "Описание пятого сабтаска", Status.NEW, 0);

        taskManager.createEpic(epic0);
        taskManager.createSubTask(subTask0);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);
        taskManager.createSubTask(subTask4);

        assertEquals(Status.NEW, taskManager.getEpicById(0).getStatus());
    }

    @Test
    void epicStatusShouldBeEqualDone() {
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        SubTask subTask0 = new SubTask("Заголовок первого сабтаска", "Описание первого сабтаска", Status.DONE, 0);
        SubTask subTask1 = new SubTask("Заголовок второго сабтаска", "Описание второго сабтаска", Status.DONE, 0);
        SubTask subTask2 = new SubTask("Заголовок третьего сабтаска", "Описание третьего сабтаска", Status.DONE, 0);
        SubTask subTask3 = new SubTask("Заголовок четвёртого сабтаска", "Описание четвёртого сабтаска", Status.DONE, 0);
        SubTask subTask4 = new SubTask("Заголовок пятого сабтаска", "Описание пятого сабтаска", Status.DONE, 0);

        taskManager.createEpic(epic0);
        taskManager.createSubTask(subTask0);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);
        taskManager.createSubTask(subTask4);

        assertEquals(Status.DONE, taskManager.getEpicById(0).getStatus());
    }

    @Test
    void epicStatusShouldBeEqualInProgress() {
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        SubTask subTask0 = new SubTask("Заголовок первого сабтаска", "Описание первого сабтаска", Status.DONE, 0);
        SubTask subTask1 = new SubTask("Заголовок второго сабтаска", "Описание второго сабтаска", Status.DONE, 0);
        SubTask subTask2 = new SubTask("Заголовок третьего сабтаска", "Описание третьего сабтаска", Status.DONE, 0);
        SubTask subTask3 = new SubTask("Заголовок четвёртого сабтаска", "Описание четвёртого сабтаска", Status.NEW, 0);
        SubTask subTask4 = new SubTask("Заголовок пятого сабтаска", "Описание пятого сабтаска", Status.NEW, 0);

        taskManager.createEpic(epic0);
        taskManager.createSubTask(subTask0);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);
        taskManager.createSubTask(subTask4);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpicById(0).getStatus());
    }

    @Test
    void epicStatusShouldBeEqualInProgressAllSubtasksAreInProgress() {
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        SubTask subTask0 = new SubTask("Заголовок первого сабтаска", "Описание первого сабтаска", Status.IN_PROGRESS, 0);
        SubTask subTask1 = new SubTask("Заголовок второго сабтаска", "Описание второго сабтаска", Status.IN_PROGRESS, 0);
        SubTask subTask2 = new SubTask("Заголовок третьего сабтаска", "Описание третьего сабтаска", Status.IN_PROGRESS, 0);
        SubTask subTask3 = new SubTask("Заголовок четвёртого сабтаска", "Описание четвёртого сабтаска", Status.IN_PROGRESS, 0);
        SubTask subTask4 = new SubTask("Заголовок пятого сабтаска", "Описание пятого сабтаска", Status.IN_PROGRESS, 0);

        taskManager.createEpic(epic0);
        taskManager.createSubTask(subTask0);
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        taskManager.createSubTask(subTask3);
        taskManager.createSubTask(subTask4);

        assertEquals(Status.IN_PROGRESS, taskManager.getEpicById(0).getStatus());
    }

    //New Tests For InMemoryHistoryManager to check

    @Test
    void shouldRemoveFromHistoryAllTasksButReturnEpicsAndSubTasks() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Заголовок третьего таска", "Описание третьего таска", Status.NEW);
        taskManager.createTask(task2);
        Task task3 = new Task("Заголовок четвёртого таска", "Описание четвёртого таска", Status.NEW);
        taskManager.createTask(task3);
        Task task4 = new Task("Заголовок пятого таска", "Описание пятого таска", Status.NEW);
        taskManager.createTask(task4);
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        taskManager.createEpic(epic0);
        Epic epic1 = new Epic("Заголовок второго эпика", "Описание второго эпика");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Заголовок третьего эпика", "Описание третьего эпика");
        taskManager.createEpic(epic2);
        Epic epic3 = new Epic("Заголовок четвёртого эпика", "Описание четвёртого эпика");
        taskManager.createEpic(epic3);
        Epic epic4 = new Epic("Заголовок пятого эпика", "Описание пятого эпика");
        taskManager.createEpic(epic4);
        SubTask subTask0 = new SubTask("Заголовок первого сабтаска", "Описание первого сабтаска", Status.NEW, 5);
        taskManager.createSubTask(subTask0);
        SubTask subTask1 = new SubTask("Заголовок второго сабтаска", "Описание второго сабтаска", Status.NEW, 6);
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("Заголовок третьего сабтаска", "Описание третьего сабтаска", Status.NEW, 7);
        taskManager.createSubTask(subTask2);
        SubTask subTask3 = new SubTask("Заголовок четвёртого сабтаска", "Описание четвёртого сабтаска", Status.NEW, 8);
        taskManager.createSubTask(subTask3);
        SubTask subTask4 = new SubTask("Заголовок пятого сабтаска", "Описание пятого сабтаска", Status.NEW, 9);
        taskManager.createSubTask(subTask4);
        taskManager.getTaskById(0);
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(3);
        taskManager.getTaskById(4);
        taskManager.getEpicById(5);
        taskManager.getEpicById(6);
        taskManager.getEpicById(7);
        taskManager.getEpicById(8);
        taskManager.getEpicById(9);
        taskManager.getSubTaskById(10);
        taskManager.getSubTaskById(11);
        taskManager.getSubTaskById(12);
        taskManager.getSubTaskById(13);
        taskManager.getSubTaskById(14);

        taskManager.deleteAllTasks();
        List<Task> historyList = taskManager.getHistory();

        assertEquals(5, historyList.get(0).getId());
        assertEquals(6, historyList.get(1).getId());
        assertEquals(7, historyList.get(2).getId());
        assertEquals(8, historyList.get(3).getId());
        assertEquals(9, historyList.get(4).getId());
        assertEquals(10, historyList.get(5).getId());
        assertEquals(11, historyList.get(6).getId());
        assertEquals(12, historyList.get(7).getId());
        assertEquals(13, historyList.get(8).getId());
        assertEquals(14, historyList.get(9).getId());
    }

    @Test
    void shouldRemoveFromHistoryAllSubTasksButReturnEpicsAndTasks() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Заголовок третьего таска", "Описание третьего таска", Status.NEW);
        taskManager.createTask(task2);
        Task task3 = new Task("Заголовок четвёртого таска", "Описание четвёртого таска", Status.NEW);
        taskManager.createTask(task3);
        Task task4 = new Task("Заголовок пятого таска", "Описание пятого таска", Status.NEW);
        taskManager.createTask(task4);
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        taskManager.createEpic(epic0);
        Epic epic1 = new Epic("Заголовок второго эпика", "Описание второго эпика");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Заголовок третьего эпика", "Описание третьего эпика");
        taskManager.createEpic(epic2);
        Epic epic3 = new Epic("Заголовок четвёртого эпика", "Описание четвёртого эпика");
        taskManager.createEpic(epic3);
        Epic epic4 = new Epic("Заголовок пятого эпика", "Описание пятого эпика");
        taskManager.createEpic(epic4);
        SubTask subTask0 = new SubTask("Заголовок первого сабтаска", "Описание первого сабтаска", Status.NEW, 5);
        taskManager.createSubTask(subTask0);
        SubTask subTask1 = new SubTask("Заголовок второго сабтаска", "Описание второго сабтаска", Status.NEW, 6);
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("Заголовок третьего сабтаска", "Описание третьего сабтаска", Status.NEW, 7);
        taskManager.createSubTask(subTask2);
        SubTask subTask3 = new SubTask("Заголовок четвёртого сабтаска", "Описание четвёртого сабтаска", Status.NEW, 8);
        taskManager.createSubTask(subTask3);
        SubTask subTask4 = new SubTask("Заголовок пятого сабтаска", "Описание пятого сабтаска", Status.NEW, 9);
        taskManager.createSubTask(subTask4);
        taskManager.getTaskById(0);
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(3);
        taskManager.getTaskById(4);
        taskManager.getEpicById(5);
        taskManager.getEpicById(6);
        taskManager.getEpicById(7);
        taskManager.getEpicById(8);
        taskManager.getEpicById(9);
        taskManager.getSubTaskById(10);
        taskManager.getSubTaskById(11);
        taskManager.getSubTaskById(12);
        taskManager.getSubTaskById(13);
        taskManager.getSubTaskById(14);

        taskManager.deleteAllSubTasks();
        List<Task> historyList = taskManager.getHistory();

        assertEquals(0, historyList.get(0).getId());
        assertEquals(1, historyList.get(1).getId());
        assertEquals(2, historyList.get(2).getId());
        assertEquals(3, historyList.get(3).getId());
        assertEquals(4, historyList.get(4).getId());
        assertEquals(5, historyList.get(5).getId());
        assertEquals(6, historyList.get(6).getId());
        assertEquals(7, historyList.get(7).getId());
        assertEquals(8, historyList.get(8).getId());
        assertEquals(9, historyList.get(9).getId());
    }

    @Test
    void shouldRemoveFromHistoryAllEpicsAndSubTasksButReturnTasks() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Заголовок третьего таска", "Описание третьего таска", Status.NEW);
        taskManager.createTask(task2);
        Task task3 = new Task("Заголовок четвёртого таска", "Описание четвёртого таска", Status.NEW);
        taskManager.createTask(task3);
        Task task4 = new Task("Заголовок пятого таска", "Описание пятого таска", Status.NEW);
        taskManager.createTask(task4);
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        taskManager.createEpic(epic0);
        Epic epic1 = new Epic("Заголовок второго эпика", "Описание второго эпика");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Заголовок третьего эпика", "Описание третьего эпика");
        taskManager.createEpic(epic2);
        Epic epic3 = new Epic("Заголовок четвёртого эпика", "Описание четвёртого эпика");
        taskManager.createEpic(epic3);
        Epic epic4 = new Epic("Заголовок пятого эпика", "Описание пятого эпика");
        taskManager.createEpic(epic4);
        SubTask subTask0 = new SubTask("Заголовок первого сабтаска", "Описание первого сабтаска", Status.NEW, 5);
        taskManager.createSubTask(subTask0);
        SubTask subTask1 = new SubTask("Заголовок второго сабтаска", "Описание второго сабтаска", Status.NEW, 6);
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("Заголовок третьего сабтаска", "Описание третьего сабтаска", Status.NEW, 7);
        taskManager.createSubTask(subTask2);
        SubTask subTask3 = new SubTask("Заголовок четвёртого сабтаска", "Описание четвёртого сабтаска", Status.NEW, 8);
        taskManager.createSubTask(subTask3);
        SubTask subTask4 = new SubTask("Заголовок пятого сабтаска", "Описание пятого сабтаска", Status.NEW, 9);
        taskManager.createSubTask(subTask4);
        taskManager.getTaskById(0);
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(3);
        taskManager.getTaskById(4);
        taskManager.getEpicById(5);
        taskManager.getEpicById(6);
        taskManager.getEpicById(7);
        taskManager.getEpicById(8);
        taskManager.getEpicById(9);
        taskManager.getSubTaskById(10);
        taskManager.getSubTaskById(11);
        taskManager.getSubTaskById(12);
        taskManager.getSubTaskById(13);
        taskManager.getSubTaskById(14);

        taskManager.deleteAllEpics();
        List<Task> historyList = taskManager.getHistory();

        assertEquals(0, historyList.get(0).getId());
        assertEquals(1, historyList.get(1).getId());
        assertEquals(2, historyList.get(2).getId());
        assertEquals(3, historyList.get(3).getId());
        assertEquals(4, historyList.get(4).getId());
    }

    @Test
    void shouldReturnListOfHistoryWithSixNodesAfterRemovingFewTasks() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Заголовок третьего таска", "Описание третьего таска", Status.NEW);
        taskManager.createTask(task2);
        Task task3 = new Task("Заголовок четвёртого таска", "Описание четвёртого таска", Status.NEW);
        taskManager.createTask(task3);
        Task task4 = new Task("Заголовок пятого таска", "Описание пятого таска", Status.NEW);
        taskManager.createTask(task4);
        Task task5 = new Task("Заголовок шестого таска", "Описание шестого таска", Status.NEW);
        taskManager.createTask(task5);
        Task task6 = new Task("Заголовок седьмого таска", "Описание седьмого таска", Status.NEW);
        taskManager.createTask(task6);
        Task task7 = new Task("Заголовок восьмого таска", "Описание восьмого таска", Status.NEW);
        taskManager.createTask(task7);
        Task task8 = new Task("Заголовок девятого таска", "Описание девятого таска", Status.NEW);
        taskManager.createTask(task8);
        Task task9 = new Task("Заголовок десятого таска", "Описание десятого таска", Status.NEW);
        taskManager.createTask(task9);
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        taskManager.createEpic(epic0);
        Epic epic1 = new Epic("Заголовок второго эпика", "Описание второго эпика");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Заголовок третьего эпика", "Описание третьего эпика");
        taskManager.createEpic(epic2);
        taskManager.getEpicById(10);
        taskManager.getEpicById(11);
        taskManager.getEpicById(12);
        taskManager.getTaskById(0);
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(3);
        taskManager.getTaskById(4);
        taskManager.getTaskById(5);
        taskManager.getTaskById(6);
        taskManager.getTaskById(7);
        taskManager.getTaskById(8);
        taskManager.getTaskById(9);

        taskManager.deleteAllEpics();
        taskManager.deleteTaskById(0);
        taskManager.deleteTaskById(1);
        taskManager.deleteTaskById(7);
        taskManager.deleteTaskById(9);
        List<Task> historyList = taskManager.getHistory();

        assertEquals(2, historyList.get(0).getId());
        assertEquals(3, historyList.get(1).getId());
        assertEquals(4, historyList.get(2).getId());
        assertEquals(5, historyList.get(3).getId());
        assertEquals(6, historyList.get(4).getId());
        assertEquals(8, historyList.get(5).getId());

    }

    @Test
    void shouldReturnHistoryListWithTwoEpicsAndTwoSubTasks() {
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        taskManager.createEpic(epic0);
        Epic epic1 = new Epic("Заголовок второго эпика", "Описание второго эпика");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Заголовок третьего эпика", "Описание третьего эпика");
        taskManager.createEpic(epic2);
        SubTask subTask1 = new SubTask("Придумать тест к методу сабтаска",
                "Написать тест к методу createSubTask", Status.IN_PROGRESS, epic0.getId());
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("Придумать тест к методу эпика",
                "Написать тест к методу createEpic", Status.DONE, epic1.getId());
        taskManager.createSubTask(subTask2);
        SubTask subTask3 = new SubTask("Придумать тест к методу эпика",
                "Написать тест к методу createEpic", Status.DONE, epic2.getId());
        taskManager.createSubTask(subTask3);
        taskManager.getEpicById(0);
        taskManager.getEpicById(1);
        taskManager.getEpicById(2);
        taskManager.getSubTaskById(3);
        taskManager.getSubTaskById(4);
        taskManager.getSubTaskById(5);

        taskManager.deleteSubTaskById(3);
        taskManager.deleteEpicById(1);
        List<Task> historyList = taskManager.getHistory();

        assertEquals(0, historyList.get(0).getId());
        assertEquals(2, historyList.get(1).getId());
        assertEquals(5, historyList.get(2).getId());
    }

    @Test
    void taskInHistoryShouldHasOldData() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        task0 = taskManager.createTask(task0);

        taskManager.getTaskById(0);
        task0.setName("Другой заголовок");
        task0.setDescription("Другое описание");
        task0.setStatus(Status.DONE);
        taskManager.updateTask(task0);
        Task taskFromHistory = taskManager.getHistory().get(0);

        Assertions.assertFalse(taskManager.getTaskById(0).getName().equals(taskFromHistory.getName()));
        Assertions.assertFalse(taskManager.getTaskById(0).getDescription().equals(taskFromHistory.getDescription()));
        Assertions.assertFalse(taskManager.getTaskById(0).getStatus() == taskFromHistory.getStatus());
        assertEquals("Заголовок первого таска", taskFromHistory.getName());
        assertEquals("Описание первого таска", taskFromHistory.getDescription());
        assertEquals(Status.NEW, taskFromHistory.getStatus());
        assertEquals(0, taskFromHistory.getId());

    }

    @Test
    void historyShouldBeEmpty() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Заголовок третьего таска", "Описание третьего таска", Status.NEW);
        taskManager.createTask(task2);
        Task task3 = new Task("Заголовок четвёртого таска", "Описание четвёртого таска", Status.NEW);
        taskManager.createTask(task3);
        Task task4 = new Task("Заголовок пятого таска", "Описание пятого таска", Status.NEW);
        taskManager.createTask(task4);
        Task task5 = new Task("Заголовок шестого таска", "Описание шестого таска", Status.NEW);
        taskManager.createTask(task5);
        Task task6 = new Task("Заголовок седьмого таска", "Описание седьмого таска", Status.NEW);
        taskManager.createTask(task6);
        Task task7 = new Task("Заголовок восьмого таска", "Описание восьмого таска", Status.NEW);
        taskManager.createTask(task7);
        Task task8 = new Task("Заголовок девятого таска", "Описание девятого таска", Status.NEW);
        taskManager.createTask(task8);
        Task task9 = new Task("Заголовок десятого таска", "Описание десятого таска", Status.NEW);
        taskManager.createTask(task9);
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        taskManager.createEpic(epic0);
        Epic epic1 = new Epic("Заголовок второго эпика", "Описание второго эпика");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Заголовок третьего эпика", "Описание третьего эпика");

        List<Task> historyList = taskManager.getHistory();

        Assertions.assertTrue(historyList.isEmpty());
    }

    @Test
    void updatedTaskShouldBeUpdatedInHistoryAfterAnotherGetTaskByIdMethodCall() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        task0 = taskManager.createTask(task0);

        taskManager.getTaskById(0);
        task0.setName("Другой заголовок");
        task0.setDescription("Другое описание");
        task0.setStatus(Status.DONE);
        taskManager.updateTask(task0);
        task0 = taskManager.getTaskById(0);
        Task taskFromHistory = taskManager.getHistory().get(0);

        assertTrue(task0.getName().equals(taskFromHistory.getName()));
        assertTrue(task0.getDescription().equals(taskFromHistory.getDescription()));
        assertTrue(task0.getStatus() == taskFromHistory.getStatus());
        assertTrue(task0.getId() == taskFromHistory.getId());
    }

    @Test
    void afterDeletingEpicItsSubTasksShouldBeDeletedTooFromHistrory() {
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        taskManager.createEpic(epic0);
        SubTask subTask0 = new SubTask("Заголовок первого сабтаска", "Описание первого сабтаска", Status.NEW, 2);
        taskManager.createSubTask(subTask0);
        SubTask subTask1 = new SubTask("Заголовок второго сабтаска", "Описание второго сабтаска", Status.NEW, 2);
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("Заголовок третьего сабтаска", "Описание третьего сабтаска", Status.NEW, 2);
        taskManager.createSubTask(subTask2);
        taskManager.getSubTaskById(1);
        taskManager.getEpicById(0);
        taskManager.getSubTaskById(3);
        taskManager.getSubTaskById(2);

        taskManager.deleteEpicById(0);

        assertTrue(taskManager.getHistory().equals(Collections.emptyList()));

    }

    @Test
    void firstTaskFromHistoryShouldBeRemoved() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Заголовок третьего таска", "Описание третьего таска", Status.NEW);
        taskManager.createTask(task2);
        Task task3 = new Task("Заголовок четвёртого таска", "Описание четвёртого таска", Status.NEW);
        taskManager.createTask(task3);
        Task task4 = new Task("Заголовок пятого таска", "Описание пятого таска", Status.NEW);
        taskManager.createTask(task4);
        Task task5 = new Task("Заголовок шестого таска", "Описание шестого таска", Status.NEW);
        taskManager.createTask(task5);
        Task task6 = new Task("Заголовок седьмого таска", "Описание седьмого таска", Status.NEW);
        taskManager.createTask(task6);
        Task task7 = new Task("Заголовок восьмого таска", "Описание восьмого таска", Status.NEW);
        taskManager.createTask(task7);
        Task task8 = new Task("Заголовок девятого таска", "Описание девятого таска", Status.NEW);
        taskManager.createTask(task8);
        Task task9 = new Task("Заголовок десятого таска", "Описание десятого таска", Status.NEW);
        taskManager.createTask(task9);
        ArrayList<Task> expectedList = new ArrayList<>(Arrays.asList(task1, task2, task3, task4, task5, task6, task7, task8, task9));

        taskManager.getTaskById(0);
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(3);
        taskManager.getTaskById(4);
        taskManager.getTaskById(5);
        taskManager.getTaskById(6);
        taskManager.getTaskById(7);
        taskManager.getTaskById(8);
        taskManager.getTaskById(9);

        taskManager.deleteTaskById(0);
        List<Task> historyList = taskManager.getHistory();

        assertEquals(expectedList, historyList);
    }

    @Test
    void fifthTaskFromHistoryShouldBeRemoved() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Заголовок третьего таска", "Описание третьего таска", Status.NEW);
        taskManager.createTask(task2);
        Task task3 = new Task("Заголовок четвёртого таска", "Описание четвёртого таска", Status.NEW);
        taskManager.createTask(task3);
        Task task4 = new Task("Заголовок пятого таска", "Описание пятого таска", Status.NEW);
        taskManager.createTask(task4);
        Task task5 = new Task("Заголовок шестого таска", "Описание шестого таска", Status.NEW);
        taskManager.createTask(task5);
        Task task6 = new Task("Заголовок седьмого таска", "Описание седьмого таска", Status.NEW);
        taskManager.createTask(task6);
        Task task7 = new Task("Заголовок восьмого таска", "Описание восьмого таска", Status.NEW);
        taskManager.createTask(task7);
        Task task8 = new Task("Заголовок девятого таска", "Описание девятого таска", Status.NEW);
        taskManager.createTask(task8);
        Task task9 = new Task("Заголовок десятого таска", "Описание десятого таска", Status.NEW);
        taskManager.createTask(task9);
        ArrayList<Task> expectedList = new ArrayList<>(Arrays.asList(task0, task1, task2, task3, task5, task6, task7, task8, task9));

        taskManager.getTaskById(0);
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(3);
        taskManager.getTaskById(4);
        taskManager.getTaskById(5);
        taskManager.getTaskById(6);
        taskManager.getTaskById(7);
        taskManager.getTaskById(8);
        taskManager.getTaskById(9);

        taskManager.deleteTaskById(4);
        List<Task> historyList = taskManager.getHistory();

        assertEquals(expectedList, historyList);
    }

    @Test
    void theLastTaskFromHistoryShouldBeRemoved() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW);
        taskManager.createTask(task1);
        Task task2 = new Task("Заголовок третьего таска", "Описание третьего таска", Status.NEW);
        taskManager.createTask(task2);
        Task task3 = new Task("Заголовок четвёртого таска", "Описание четвёртого таска", Status.NEW);
        taskManager.createTask(task3);
        Task task4 = new Task("Заголовок пятого таска", "Описание пятого таска", Status.NEW);
        taskManager.createTask(task4);
        Task task5 = new Task("Заголовок шестого таска", "Описание шестого таска", Status.NEW);
        taskManager.createTask(task5);
        Task task6 = new Task("Заголовок седьмого таска", "Описание седьмого таска", Status.NEW);
        taskManager.createTask(task6);
        Task task7 = new Task("Заголовок восьмого таска", "Описание восьмого таска", Status.NEW);
        taskManager.createTask(task7);
        Task task8 = new Task("Заголовок девятого таска", "Описание девятого таска", Status.NEW);
        taskManager.createTask(task8);
        Task task9 = new Task("Заголовок десятого таска", "Описание десятого таска", Status.NEW);
        taskManager.createTask(task9);
        ArrayList<Task> expectedList = new ArrayList<>(Arrays.asList(task0, task1, task2, task3, task4, task5, task6, task7, task8));

        taskManager.getTaskById(0);
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(3);
        taskManager.getTaskById(4);
        taskManager.getTaskById(5);
        taskManager.getTaskById(6);
        taskManager.getTaskById(7);
        taskManager.getTaskById(8);
        taskManager.getTaskById(9);

        taskManager.deleteTaskById(9);
        List<Task> historyList = taskManager.getHistory();

        assertEquals(expectedList, historyList);
    }

    @Test
    void afterSubTasksDeletingItsIdsAlsoShouldBeRemovedFromEpicsList() {
        Epic epic0 = new Epic("Заголовок первого эпика", "Описание первого эпика");
        taskManager.createEpic(epic0);
        SubTask subTask0 = new SubTask("Заголовок первого сабтаска", "Описание первого сабтаска", Status.NEW, 0);
        taskManager.createSubTask(subTask0);
        SubTask subTask1 = new SubTask("Заголовок второго сабтаска", "Описание второго сабтаска", Status.NEW, 0);
        taskManager.createSubTask(subTask1);
        SubTask subTask2 = new SubTask("Заголовок третьего сабтаска", "Описание третьего сабтаска", Status.NEW, 0);
        taskManager.createSubTask(subTask2);
        SubTask subTask3 = new SubTask("Заголовок четвёртого сабтаска", "Описание четвёртого сабтаска", Status.NEW, 0);
        taskManager.createSubTask(subTask3);
        SubTask subTask4 = new SubTask("Заголовок пятого сабтаска", "Описание пятого сабтаска", Status.NEW, 0);
        taskManager.createSubTask(subTask4);

        taskManager.deleteSubTaskById(2);
        taskManager.deleteSubTaskById(5);

        assertTrue(new ArrayList<>(Arrays.asList(1, 3, 4)).equals(epic0.getSubTasksIds()));
    }

    @Test
    void userHaveNoAccessToTaskManagerDataTaskInManagerWillNotChange() {
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW);
        task0 = taskManager.createTask(task0);

        task0.setName("Заголовок, который указал пользователь");
        task0.setDescription("Описание, которое указал пользователь");
        task0.setStatus(Status.IN_PROGRESS);
        task0.setId(666);
        Task taskFromManager = taskManager.getTaskById(0);

        Assertions.assertFalse(task0.getName() == taskFromManager.getName());
        Assertions.assertFalse(task0.getDescription() == taskFromManager.getDescription());
        Assertions.assertFalse(task0.getStatus() == taskFromManager.getStatus());
        Assertions.assertFalse(task0.getId() == taskFromManager.getId());
    }

    @Test
    void shouldBeAnIntersectionOfTwoTasksInTheCompletionIntervals() {
        long seconds0 = LocalDateTime.of(2025, 4, 2, 12, 0, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime0 = Instant.ofEpochSecond(seconds0);
        long seconds1 = LocalDateTime.of(2025, 4, 2, 12, 15, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime1 = Instant.ofEpochSecond(seconds1);
        long seconds2 = LocalDateTime.of(2025, 4, 2, 12, 30, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime2 = Instant.ofEpochSecond(seconds2);
        long seconds3 = LocalDateTime.of(2025, 4, 2, 12, 45, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime3 = Instant.ofEpochSecond(seconds3);
        long seconds4 = LocalDateTime.of(2025, 4, 2, 13, 0, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime4 = Instant.ofEpochSecond(seconds4);
        long seconds5 = LocalDateTime.of(2025, 4, 2, 13, 7, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime5 = Instant.ofEpochSecond(seconds5);
        Duration duration = Duration.ofMinutes(15);
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW, startTime0, duration);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW, startTime1, duration);
        taskManager.createTask(task1);
        Task task2 = new Task("Заголовок третьего таска", "Описание третьего таска", Status.NEW, startTime2, duration);
        taskManager.createTask(task2);
        Task task3 = new Task("Заголовок четвёртого таска", "Описание четвёртого таска", Status.NEW, startTime3, duration);
        taskManager.createTask(task3);
        Task task4 = new Task("Заголовок пятого таска", "Описание пятого таска", Status.NEW, startTime4, duration);
        taskManager.createTask(task4);
        Task task5 = new Task("Заголовок шестого таска", "Описание шестого таска", Status.NEW, startTime5, duration);
        String errMessage = "Указанная задача: \"Заголовок шестого таска\" с \n"
                + "датой начала: 13:07, 02.04.2025\n"
                + "продолжительностью в минутах: 15\n"
                + "пересекается с одной из уже добавленных раннее задач:\n"
                + "\"Заголовок пятого таска\"\n"
                + "дата начала: 13:00, 02.04.2025\n"
                + "продолжительность в минутах: 15\n\n";

        PrintStream originalOut = System.out;
        try {
            ByteArrayOutputStream outputCaptor = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputCaptor));

            taskManager.createTask(task5);

            String consoleOutput = outputCaptor.toString();
            assertEquals(errMessage, consoleOutput);
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void shouldBeTheTimeIntervalOfOneTaskIsInsideTheIntervalOfAnother() {
        long seconds0 = LocalDateTime.of(2025, 4, 2, 12, 0, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime0 = Instant.ofEpochSecond(seconds0);
        long seconds1 = LocalDateTime.of(2025, 4, 2, 12, 7, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime1 = Instant.ofEpochSecond(seconds1);
        long seconds2 = LocalDateTime.of(2025, 4, 2, 12, 30, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime2 = Instant.ofEpochSecond(seconds2);
        long seconds3 = LocalDateTime.of(2025, 4, 2, 12, 45, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime3 = Instant.ofEpochSecond(seconds3);
        long seconds4 = LocalDateTime.of(2025, 4, 2, 13, 0, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime4 = Instant.ofEpochSecond(seconds4);
        long seconds5 = LocalDateTime.of(2025, 4, 2, 13, 15, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime5 = Instant.ofEpochSecond(seconds5);
        Duration duration = Duration.ofMinutes(15);
        Duration durationOfTask1 = Duration.ofMinutes(2);
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW, startTime0, duration);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW, startTime1, durationOfTask1);


        String errMessage = "Указанная задача: \"Заголовок второго таска\" с \n"
                + "датой начала: 12:07, 02.04.2025\n"
                + "продолжительностью в минутах: 2\n"
                + "пересекается с одной из уже добавленных раннее задач:\n"
                + "\"Заголовок первого таска\"\n"
                + "дата начала: 12:00, 02.04.2025\n"
                + "продолжительность в минутах: 15\n\n";

        PrintStream originalOut = System.out;
        try {
            ByteArrayOutputStream outputCaptor = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outputCaptor));

            taskManager.createTask(task1);

            String consoleOutput = outputCaptor.toString();
            assertEquals(errMessage, consoleOutput);
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void shouldReturnSortedByTimeListOfTasks() {
        long seconds0 = LocalDateTime.of(2026, 4, 2, 12, 0, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime0 = Instant.ofEpochSecond(seconds0);
        long seconds1 = LocalDateTime.of(2025, 4, 2, 12, 15, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime1 = Instant.ofEpochSecond(seconds1);
        long seconds2 = LocalDateTime.of(2025, 5, 2, 12, 30, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime2 = Instant.ofEpochSecond(seconds2);
        long seconds3 = LocalDateTime.of(2025, 4, 2, 12, 45, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime3 = Instant.ofEpochSecond(seconds3);
        long seconds4 = LocalDateTime.of(2025, 4, 3, 13, 0, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime4 = Instant.ofEpochSecond(seconds4);
        long seconds5 = LocalDateTime.of(2025, 4, 2, 13, 45, 0)
                .atZone(ZoneOffset.UTC)
                .toEpochSecond();
        Instant startTime5 = Instant.ofEpochSecond(seconds5);
        Duration duration = Duration.ofMinutes(15);
        Task task0 = new Task("Заголовок первого таска", "Описание первого таска", Status.NEW, startTime0, duration);
        taskManager.createTask(task0);
        Task task1 = new Task("Заголовок второго таска", "Описание второго таска", Status.NEW, startTime1, duration);
        taskManager.createTask(task1);
        Task task2 = new Task("Заголовок третьего таска", "Описание третьего таска", Status.NEW, startTime2, duration);
        taskManager.createTask(task2);
        Task task3 = new Task("Заголовок четвёртого таска", "Описание четвёртого таска", Status.NEW, startTime3, duration);
        taskManager.createTask(task3);
        Task task4 = new Task("Заголовок пятого таска", "Описание пятого таска", Status.NEW, startTime4, duration);
        taskManager.createTask(task4);
        Task task5 = new Task("Заголовок шестого таска", "Описание шестого таска", Status.NEW, startTime5, duration);
        taskManager.createTask(task5);
        ArrayList<Task> expectedListOfSortedByTimeTasks = new ArrayList<>();
        expectedListOfSortedByTimeTasks.add(task1);
        expectedListOfSortedByTimeTasks.add(task3);
        expectedListOfSortedByTimeTasks.add(task5);
        expectedListOfSortedByTimeTasks.add(task4);
        expectedListOfSortedByTimeTasks.add(task2);
        expectedListOfSortedByTimeTasks.add(task0);

        ArrayList<Task> listOfSortedByTimeTasks = taskManager.getPrioritizedTasks();

        Assertions.assertEquals(expectedListOfSortedByTimeTasks, listOfSortedByTimeTasks);
    }
}