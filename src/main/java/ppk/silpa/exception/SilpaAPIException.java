package ppk.silpa.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class SilpaAPIException extends RuntimeException {

    private final HttpStatus status;
    private final String message;

    public SilpaAPIException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
