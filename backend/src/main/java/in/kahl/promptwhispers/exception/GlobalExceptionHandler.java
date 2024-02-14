package in.kahl.promptwhispers.exception;

import in.kahl.promptwhispers.model.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(GoogleEmailNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleGoogleEmailNotFound(GoogleEmailNotFoundException ex) {
        return new ErrorMessage("GoogleEmailNotFoundException: " + ex.getMessage());
    }
}
