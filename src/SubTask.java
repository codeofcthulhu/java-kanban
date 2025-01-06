public class SubTask extends Task {
    private Integer epic_ID;

    public SubTask(String description, String name, int id, Status status, Integer epic_ID) {
        super(description, name, id, status);
        this.epic_ID = epic_ID;
    }
}
