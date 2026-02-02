package exceptions;

public class TaskIdIsIncorrectException extends RuntimeException {

    public TaskIdIsIncorrectException(String message) {
        super(message);
    }
}
