package in.kahl.promptwhispers.exception;

public class GoogleEmailNotFoundException extends RuntimeException {
    public GoogleEmailNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
