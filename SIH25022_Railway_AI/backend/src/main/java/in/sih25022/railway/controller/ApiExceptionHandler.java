package in.sih25022.railway.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error",e.getMessage()));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));
    }
}
