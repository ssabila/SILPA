package ppk.silpa.exception;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponEksepsi> tanganiResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponEksepsi(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(SilpaAPIException.class)
    public ResponseEntity<ResponEksepsi> tanganiSilpaAPIException(SilpaAPIException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ResponEksepsi(
                        ex.getStatus().value(),
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ResponEksepsi> tanganiFileStorageException(FileStorageException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponEksepsi(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        ex.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponEksepsi> tanganiValidasiException(MethodArgumentNotValidException ex) {
        Map<String, String> galat = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                galat.put(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ResponEksepsi(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validasi gagal",
                        LocalDateTime.now(),
                        galat
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponEksepsi> tanganiEksepsiUmum(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponEksepsi(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Terjadi kesalahan pada server",
                        LocalDateTime.now()
                ));
    }

    @Data
    public static class ResponEksepsi {
        private int status;
        private String pesan;
        private LocalDateTime waktu;
        private Map<String, String> galat;

        public ResponEksepsi(int status, String pesan, LocalDateTime waktu) {
            this.status = status;
            this.pesan = pesan;
            this.waktu = waktu;
        }

        public ResponEksepsi(int status, String pesan, LocalDateTime waktu, Map<String, String> galat) {
            this.status = status;
            this.pesan = pesan;
            this.waktu = waktu;
            this.galat = galat;
        }
    }
}
