package ru.practicum.shareit.error_handler;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ShareitException extends RuntimeException {

    private final HttpStatus status;
    private final String message;
}
