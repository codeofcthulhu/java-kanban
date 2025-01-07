package tasks;

public class SubTask extends Task {
    private Integer epic_ID;

    public SubTask(String name, String description, Status status, int id,  Integer epic_ID) {
        super(name, description, status, id);
        this.epic_ID = epic_ID;
    }


    public SubTask(SubTask subTask, int id) {
        super(subTask.name, subTask.description, subTask.status);
        this.epic_ID = subTask.epic_ID;
        this.id = id;
    }

    public SubTask(String name, String description, Status status, Integer epic_ID) {
        super(name, description, status);
        this.epic_ID = epic_ID;
    }

    public Integer getEpic_ID() {
        return epic_ID;
    }

    public void setEpic_ID(Integer epic_ID) {
        this.epic_ID = epic_ID;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", id=" + id +
                " epic_ID=" + epic_ID +
                '}';
    }
}
