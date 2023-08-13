package ru.practicum.shareit.error_handler;

import org.springframework.http.HttpStatus;

public class ShareitInvalidArgumentException extends ShareitException {

    public ShareitInvalidArgumentException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
