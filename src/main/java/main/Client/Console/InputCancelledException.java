package main.Client.Console;

public class InputCancelledException extends RuntimeException {
    public InputCancelledException(String message) {
        super(message);
    }
}
