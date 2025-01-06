import java.util.ArrayList;
public class Epic extends Task {
    ArrayList<Integer> subTasksIds;

    public Epic(String description, String name, int id, Status status, ArrayList<Integer> subTasksIds) {
        super(description, name, id, status);
        this.subTasksIds = subTasksIds;
    }
}
