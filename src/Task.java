public class Task {
    private String name;
    private String description;
    private int id;
    private Status status;

    public Task(String description, String name, int id, Status status) {
        this.description = description;
        this.name = name;
        this.id = id;
        this.status = status;
    }
}
